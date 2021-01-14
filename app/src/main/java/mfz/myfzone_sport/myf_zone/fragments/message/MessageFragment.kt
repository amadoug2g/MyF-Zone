package mfz.myfzone_sport.myf_zone.fragments.message

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
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.ActivityMainScreenBinding
import mfz.myfzone_sport.myf_zone.databinding.CardMessageCoachBinding
import mfz.myfzone_sport.myf_zone.databinding.FragmentMessageBinding
import mfz.myfzone_sport.myf_zone.fragments.message.MessageService.getImageReference
import mfz.myfzone_sport.myf_zone.glide.GlideApp
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.chat.Chat
import mfz.myfzone_sport.myf_zone.util.Constants.TRACKING
import mfz.myfzone_sport.myf_zone.util.Tracking
import org.jetbrains.anko.support.v4.toast

class MessageFragment : Fragment() {
    companion object {
        private val TAG = MessageFragment::class.java.simpleName
        private var adapter: FirestoreRecyclerAdapter<Chat, ChatHolder>? = null
        private val userChat = MutableLiveData<Boolean>()

        private lateinit var binding: FragmentMessageBinding
        private lateinit var activityMainScreenBinding: ActivityMainScreenBinding
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

                userChat.value = chat.unread

//                bottomNavBar.getOrCreateBadge(R.id.message).number = 1
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
//        bottomNavBar.getOrCreateBadge(R.id.message).number = 1

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
                super.onDataChanged()
                lifecycleScope.launch {
                    viewModel.checkUserSignedIn()
                    viewModel.checkUserAffiliationStatus()
                }

                userChat.observe(viewLifecycleOwner) { userHasUnreadChat ->
                    viewModel.userHasMessages(userHasUnreadChat)
                }

                viewModel.newChat.observe(viewLifecycleOwner) { userHasChat ->
                    if (userHasChat) {
//                        bottomNavBar.getOrCreateBadge(R.id.message).number = 1
//                        MainScreen.binding.bottomNavBar.getOrCreateBadge(R.id.message).backgroundColor =
//                            ContextCompat.getColor(requireContext(), R.color.colorCoral)
                        Log.i(TAG, "has unread messages")
                    } else {
                        Log.i(TAG, "has no unread messages")
                    }
                }

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

//                MainScreen.binding.bottomNavBar.getOrCreateBadge(R.id.message).backgroundColor = ContextCompat.getColor(requireContext(), R.color.colorCoral)
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
                    val user = state.data
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