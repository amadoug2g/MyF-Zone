package com.myfzone_sport.myf_zone.app.ui.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ListenerRegistration
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.ChatViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.ChatViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentChatBinding
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.discussion.*
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.util.Tracking
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.toast

private const val ARG_PARAM1 = "coachId"

class ChatFragment : Fragment() {

    //region Variables
    companion object {
        private var coachId: String? = null

        private lateinit var binding: FragmentChatBinding
        private lateinit var viewModel: ChatViewModel
        private lateinit var viewModelFactory: ChatViewModelFactory
        private lateinit var messagesListenerRegistration: ListenerRegistration
        private var shouldInitRecycler = true
        private lateinit var messageSection: Section
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shouldInitRecycler = true
        arguments?.let {
            coachId = it.getString(ARG_PARAM1)
        }

        setupViewModel()

        lifecycleScope.launchWhenResumed {
            viewModel.assignChatCoach(coachId!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_chat,
            container,
            false
        )

        setupViews()
        setupObservers()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setDiscussionAsRead()
    }

    override fun onPause() {
        setDiscussionAsRead()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.recyclerViewMessages.hideKeyboard()
    }
    //endregion

    //region Setups
    private fun setupViewModel() {
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getChatCoachUseCase = GetChatCoachUseCase(repository)
        val getChatCoachClubUseCase = GetChatCoachClubUseCase(repository)
        val getOrCreateChatUseCase = GetOrCreateChatUseCase(repository)
        val createChatUseCase = CreateChatUseCase(repository)
        val sendChatMessageUseCase = SendChatMessageUseCase(repository)
        val setDiscussionReadUseCase = SetDiscussionReadUseCase(repository)
        val setDiscussionUnreadUseCase = SetDiscussionUnreadUseCase(repository)
        val addChatMessageListenerUseCase = AddChatMessageListenerUseCase(repository)
        val getImageReferenceUseCase = GetImageReferenceUseCase(repository)

        viewModelFactory = ChatViewModelFactory(
            getChatCoachUseCase,
            getChatCoachClubUseCase,
            getOrCreateChatUseCase,
            createChatUseCase,
            sendChatMessageUseCase,
            setDiscussionReadUseCase,
            setDiscussionUnreadUseCase,
            addChatMessageListenerUseCase,
            getImageReferenceUseCase,
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ChatViewModel::class.java)
    }

    private fun setupViews() {
        binding.backArrow.apply {
            background = null
            setOnClickListener {
                requireActivity().onBackPressed()
            }
        }

        viewModel.chatCoach.observe(viewLifecycleOwner, { coach ->
            (activity as AppCompatActivity).supportActionBar?.apply {
                binding.chatTitle.text = coach.firstName
            }

            viewModel.chatCoachAffiliation.observe(viewLifecycleOwner) { club ->
                GlideApp.with(binding.chatImage).apply {
                    load(viewModel.getImageReference(club.clubLogo))
                        .centerCrop()
                        .into(binding.chatImage)
                }

                binding.imageSenderButton.setOnClickListener {
                    TRACKING.logEvent(Tracking.CHAT_DETAILS_SEND_MESSAGE, null)
                    viewModel.getOrCreateChat(
                        coach,
                        club,
                        binding.senderTextBox.text.toString(),
                        ""
                    )
                    binding.senderTextBox.setText("")
                }
            }

            try {
                messagesListenerRegistration =
                    viewModel.addChatListener(
                        coach.id,
                        requireContext(),
                        this::updateRecyclerView
                    )!!
            } catch (e: Exception) {
//                Log.e(TAG, "Error in messageListener: $e")
                toast("Error in messageListener: $e")
            }
        })

        binding.senderTextBox.textChangedListener {
            beforeTextChanged { _, _, _, _ ->
//                viewModel.typeStart()
            }

            onTextChanged { _, _, _, _ ->
            }

            afterTextChanged {
                when (it.toString().isEmpty()) {
                    true -> {
                        binding.imageSenderButton.isEnabled = false
                    }
                    false -> {
                        binding.imageSenderButton.isEnabled = true
                    }
                }
//                viewModel.typeStop()
            }
        }

        when (binding.senderTextBox.text.isNullOrEmpty()) {
            true -> {
                binding.imageSenderButton.isEnabled = false
            }
            false -> {
                binding.imageSenderButton.isEnabled = true
            }
        }
    }

    private fun setupObservers() {
    }
    //endregion

    //region Chat
    private fun setDiscussionAsRead() {
        viewModel.chatCoach.observe(viewLifecycleOwner, {
            viewModel.setChatRead(it)
        })
    }
    //endregion

    //region RecyclerView
    private fun updateRecyclerView(messages: List<Item>) {
        fun init() {
            binding.recyclerViewMessages.apply {
                val layout = LinearLayoutManager(requireContext())
                layout.stackFromEnd = true
                layoutManager = layout
                adapter = GroupAdapter<GroupieViewHolder>().apply {
                    messageSection = Section(messages)
                    this.add(messageSection)
                }
            }
            shouldInitRecycler = false
        }

        fun updateItems() = messageSection.update(messages)

        if (shouldInitRecycler) init() else updateItems()

        binding.recyclerViewMessages.scrollToPosition(binding.recyclerViewMessages.adapter!!.itemCount - 1)
    }
    //endregion

    //region View Methods
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun showError(message: String? = "") {
        Snackbar.make(
            binding.recyclerViewMessages,
            if (!message.isNullOrEmpty()) message else viewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }
    //endregion
}