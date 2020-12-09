package mfz.myfzone_sport.myf_zone.fragments.message

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.CardMessageCoachBinding
import mfz.myfzone_sport.myf_zone.databinding.FragmentMessageBinding
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import org.jetbrains.anko.support.v4.toast

class MessageFragment : Fragment() {
    companion object {
        private val TAG = MessageFragment::class.java.simpleName
        private var adapter: FirestoreRecyclerAdapter<Coach, CoachHolder>? = null

        private lateinit var binding: FragmentMessageBinding
        private lateinit var viewModel: MessageViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")

        viewModel = ViewModelProvider(this).get(MessageViewModel::class.java)

        lifecycleScope.launch {
            viewModel.checkUserAffiliationStatus()
        }

        val recyclerOptions = FirestoreRecyclerOptions.Builder<Coach>()
            .setQuery(viewModel.query.value!!/*.orderBy("date")*/, Coach::class.java)
            .setLifecycleOwner(this)
            .build()

        adapter = object :
            FirestoreRecyclerAdapter<Coach, CoachHolder>(recyclerOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoachHolder {
                return CoachHolder.from(parent)
            }

            override fun onBindViewHolder(holder: CoachHolder, position: Int, model: Coach) {
                holder.bind(model)
            }

        }
    }

    class CoachHolder(val binding: CardMessageCoachBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(coach: Coach) {
            with(binding) {
                binding.coach = coach

//                try {
//                    val dotBg = if (viewModel.isUserSignedIn.value!!) {
//                        if (viewModel.isUserAffiliated.value!!) {
//                            R.drawable.notification_dot_green
//                        } else {
//                            R.drawable.notification_dot_blue
//                        }
//                    } else {
//                        R.drawable.notification_dot_red
//                    }
//                    binding.notificationDotOwner.setImageResource(dotBg)
//                } catch (e: Exception) {
//                    Log.e("CoachHolder", "Notification Dot could not load: $e")
//                }

                binding.messageCardViewTitle.setOnClickListener { userConversation(coach) }
            }
        }

        private fun userConversation(coach: Coach) {
            Log.d(TAG, "clicked ${coach.firstName}")
            val bundle = bundleOf("coachId" to coach.id)
            navigate(R.id.messageToDiscussion, bundle)
        }

        private fun navigate(destination: Int, extra: Bundle? = null) {
            Navigation
                .findNavController(itemView)
                .navigate(destination, extra)
        }

        companion object {
            fun from(parent: ViewGroup): CoachHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CardMessageCoachBinding.inflate(layoutInflater, parent, false)
                return CoachHolder(binding)
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

        binding.apply {
            lifecycleOwner = this@MessageFragment
//            setupRecyclerParameters()
            executePendingBindings()
        }

        binding.accountButton.background = null
        binding.accountButton.setOnClickListener {
            accountButton()
        }

        return binding.root
    }

    private fun setupRecyclerParameters() {
        binding.messageUserList.setHasFixedSize(false)
        binding.messageUserList.layoutManager = LinearLayoutManager(requireContext())
        binding.messageUserList.adapter = adapter
        binding.messageUserList.isNestedScrollingEnabled = false
    }

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

    private fun accountButton() {
        if (!viewModel.isUserSignedIn.value!!) {
            navigate(R.id.messageToLogin)
        } else {
            viewModel.isUserAffiliated.observe(viewLifecycleOwner) { isUserAffiliated ->
                when (isUserAffiliated) {
                    true -> navigate(R.id.messageToProfile)
                    false -> navigate(R.id.messageToAffiliationRequest)
                }
            }
        }
    }

    private fun navigate(destination: Int) {
        findNavController().navigate(destination)
    }


    private fun showToast(string: String) {
        toast(string)
    }
}