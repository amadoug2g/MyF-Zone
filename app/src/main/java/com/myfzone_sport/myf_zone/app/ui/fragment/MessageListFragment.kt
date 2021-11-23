package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.Query
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.MessageListViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.domain.chat.Chat
import com.myfzone_sport.myf_zone.app.ui.viewmodel.MessageListViewModel
import com.myfzone_sport.myf_zone.databinding.CardMessageListCoachBinding
import com.myfzone_sport.myf_zone.databinding.FragmentMessageListBinding
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserUseCase
import com.myfzone_sport.myf_zone.util.Tracking
import java.lang.Exception

private const val ARG_PARAM1 = "coachId"

class MessageListFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentMessageListBinding
        private lateinit var listViewModel: MessageListViewModel
        private lateinit var listViewModelFactory: MessageListViewModelFactory
        private var coachId: String? = null
        private var adapter: FirestoreRecyclerAdapter<Chat, ChatHolder>? = null
    }
    //endregion

    //region Firestore RecyclerView
    class ChatHolder(val binding: CardMessageListCoachBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            with(binding) {
                this.chat = chat

                try {
                    GlideApp.with(itemView).apply {
                        load(listViewModel.getImageReference(chat.clubLogo))
                            .centerCrop()
                            .into(binding.coachImage)
                    }
                } catch (e: Exception) {
                    Log.e("tagging", "Error in message list: $e")
                }

                this.layout.setOnClickListener {
                    TRACKING.logEvent(Tracking.CHAT_DETAILS, null)
                    userConversation(chat)
                }
            }
        }

        private fun userConversation(chat: Chat) {
            val bundle = bundleOf("coachId" to chat.coachId)
            navigate(R.id.messageListToChat, bundle)
        }

        private fun navigate(destination: Int, extra: Bundle? = null) {
            Navigation
                .findNavController(itemView)
                .navigate(destination, extra)
        }

        companion object {
            fun from(parent: ViewGroup): ChatHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CardMessageListCoachBinding.inflate(layoutInflater, parent, false)
                return ChatHolder(binding)
            }
        }
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            coachId = it.getString(ARG_PARAM1)
        }

        setupViewModel()

        listViewModel.assignCoachId(coachId!!)

        val recyclerOptions = FirestoreRecyclerOptions.Builder<Chat>()
            .setQuery(
                listViewModel.getQuery(coachId!!).orderBy("updatedDate", Query.Direction.DESCENDING),
                Chat::class.java
            )
            .setLifecycleOwner(this)
            .build()

        adapter = object : FirestoreRecyclerAdapter<Chat, ChatHolder>(recyclerOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
                return ChatHolder.from(parent)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onBindViewHolder(holder: ChatHolder, position: Int, model: Chat) {
                holder.bind(model)

                if (model.unread) {
                    holder.binding.lastDatedMessage.apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDesign2))
                        typeface = resources.getFont(R.font.gilroy_semi_bold)
                    }
//                    holder.binding.lastDatedMessage.setTextAppearance(R.font.gilroy_medium)
                } else {
                    holder.binding.lastDatedMessage.apply {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDesign1Transparent75))
                        typeface = resources.getFont(R.font.gilroy_medium)
                    }
                }
            }

            override fun onDataChanged() {
                super.onDataChanged()

                val params = binding.recyclerView.layoutParams
                params.height = 160 * itemCount
                binding.recyclerView.layoutParams = params
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_message_list, container, false
        )

        setupViews()
        setupObservers()

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViewModel() {

        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getUserUseCase = GetUserUseCase(repository)
        val getImageReferenceUseCase = GetImageReferenceUseCase(repository)

        listViewModelFactory = MessageListViewModelFactory(
            getUserUseCase,
            getImageReferenceUseCase,
        )

        listViewModel = ViewModelProvider(this, listViewModelFactory)
            .get(MessageListViewModel::class.java)
    }

    private fun setupViews() {
        binding.backArrow.apply {
            background = null
            setOnClickListener {
                requireActivity().onBackPressed()
            }
        }

        setupRecyclerView()
    }

    private fun setupObservers() {
        listViewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        listViewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })
    }
    //endregion

    //region RecyclerViews
    private fun setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(false)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.isNestedScrollingEnabled = false
    }
    //endregion

    //region Views Methods
    private fun loadingStart() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun loadingStop() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showError(message: String? = "") {
        Snackbar.make(
            binding.progressBar,
            if (!message.isNullOrEmpty()) message else listViewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }
    //endregion
}