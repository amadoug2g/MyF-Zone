package com.myfzone_sport.myf_zone.fragments.message

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.CardMessageCoachBinding
import com.myfzone_sport.myf_zone.databinding.FragmentMessageBinding
import com.myfzone_sport.myf_zone.fragments.message.MessageService.getImageReference
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.chat.Chat
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.toast

class MessageFragment : Fragment() {
    companion object {
        private val TAG = this::class.java.simpleName
        private var adapter: FirestoreRecyclerAdapter<Chat, ChatHolder>? = null

        private lateinit var binding: FragmentMessageBinding
        private lateinit var viewModel: MessageViewModel
    }

    class ChatHolder(val binding: CardMessageCoachBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            with(binding) {

                binding.chat = chat

                try {
                    GlideApp.with(itemView).apply {
                        load(getImageReference(chat.clubLogo))
                            .placeholder(R.drawable.ic_account)
                            .centerCrop()
                            .into(binding.messageCoachImage)
                    }
                } catch (e: Exception) {
                    Log.e("CoachHolder", "Image could not load: $e")
                }

                binding.messageCardViewTitle.setOnClickListener {
                    TRACKING.logEvent(Tracking.CHAT_DETAILS, null)
                    userConversation(chat)
                }
            }
        }

        private fun userConversation(chat: Chat) {
            val bundle = bundleOf("coachId" to chat.coachId)
            navigate(R.id.messageToDiscussion, bundle)
        }

        private fun navigate(destination: Int, extra: Bundle? = null) {
            Navigation
                .findNavController(itemView)
                .navigate(destination, extra)
        }

        companion object {
            fun from(parent: ViewGroup): ChatHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CardMessageCoachBinding.inflate(layoutInflater, parent, false)
                return ChatHolder(binding)
            }
        }
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")

        viewModel = ViewModelProvider(this).get(MessageViewModel::class.java)

        lifecycleScope.launch {
            viewModel.checkUserSignedIn()
            viewModel.checkUserAffiliationStatus()
        }

        val recyclerOptions = FirestoreRecyclerOptions.Builder<Chat>()
            .setQuery(
                viewModel.query.value!!.orderBy("updatedDate", Query.Direction.DESCENDING),
                Chat::class.java
            )
            .setLifecycleOwner(this)
            .build()

        adapter = object :
            FirestoreRecyclerAdapter<Chat, ChatHolder>(recyclerOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
                return ChatHolder.from(parent)
            }

            override fun onBindViewHolder(holder: ChatHolder, position: Int, model: Chat) {
                holder.bind(model)
            }

            override fun onDataChanged() {
                viewModel.isUserSignedIn.observe(viewLifecycleOwner) { isUserSignedIn ->
                    if (isUserSignedIn) {
                        viewModel.isUserAffiliated.observe(viewLifecycleOwner) { isUserAffiliated ->
                            if (isUserAffiliated) {
                                binding.messageEmptyList.visibility =
                                    if (itemCount == 0) View.VISIBLE else View.GONE

                                val params = binding.messageUserList.layoutParams
                                params.height = 320 * itemCount
                                binding.messageUserList.layoutParams = params
                            } else {
                                binding.messageChatListNotAffiliated.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        binding.messageChatListNotSignedIn.visibility = View.VISIBLE
                    }
                }
                super.onDataChanged()
                lifecycleScope.launch {
                    viewModel.checkUserSignedIn()
                    viewModel.checkUserAffiliationStatus()
                }


            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_message,
            container,
            false
        )

        lifecycleScope.launch {
            viewModel.checkUserSignedIn()
            viewModel.checkUserAffiliationStatus()
        }

        binding.apply {
            lifecycleOwner = this@MessageFragment
            setupRecyclerParameters()
            executePendingBindings()
        }

        binding.messageChatListNotAffiliated.setOnClickListener { navigate(R.id.messageToAffiliationRequest) }
        binding.messageChatListNotSignedIn.setOnClickListener { navigate(R.id.messageToSignUp) }

        return binding.root
    }
    //endregion

    //region RecyclerView
    private fun setupRecyclerParameters() {
        binding.messageUserList.setHasFixedSize(false)
        binding.messageUserList.layoutManager = LinearLayoutManager(requireContext())
        binding.messageUserList.adapter = adapter
        binding.messageUserList.isNestedScrollingEnabled = false
    }
    //endregion

    //region User Info
    private suspend fun loadUser() {
        viewModel.getCurrentUser().collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
//                    hideProgressBar()
//                    val user = state.data
//                    binding.user = user
                }
                is State.Failed -> {
//                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    showToast(message)
                }
            }
        }
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int) {
        findNavController().navigate(destination)
    }
    //endregion

    //region View Methods
    private fun showToast(string: String) {
        toast(string)
    }
    //endregion
}