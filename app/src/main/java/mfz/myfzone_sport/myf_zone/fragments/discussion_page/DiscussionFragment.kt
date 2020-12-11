package mfz.myfzone_sport.myf_zone.fragments.discussion_page

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentDiscussionBinding
import mfz.myfzone_sport.myf_zone.fragments.discussion_page.DiscussionService.addChatMessageListener
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.toast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "coachId"
private const val ARG_PARAM2 = "param2"

class DiscussionFragment : Fragment() {
    companion object {
        private val TAG = DiscussionFragment::class.java.simpleName
        private var coachId: String? = null
        private var param2: String? = null

        private lateinit var binding: FragmentDiscussionBinding
        private lateinit var viewModel: DiscussionViewModel
        private lateinit var messagesListenerRegistration: ListenerRegistration
        private var shouldInitRecycler = true
        private lateinit var messageSection: Section
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "shouldInitRecycler = $shouldInitRecycler [onCreate]")
        shouldInitRecycler = true
        arguments?.let {
            coachId = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        viewModel = ViewModelProvider(this).get(DiscussionViewModel::class.java)

        lifecycleScope.launch {
            viewModel.assignCurrentUser()
            viewModel.assignCurrentClub()
            viewModel.assignDiscussionUser(coachId!!)
            viewModel.assignOtherClub(coachId!!)
        }

        requireActivity().window.setSoftInputMode(SOFT_INPUT_ADJUST_PAN)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_discussion,
            container,
            false
        )

        //Page Title
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = viewModel.coach.value?.firstName
        }

        viewModel.other.observe(viewLifecycleOwner) { other ->
            //Page Title
            (activity as AppCompatActivity).supportActionBar?.apply {
                title = other.firstName
            }

            viewModel.otherClub.observe(viewLifecycleOwner) { club ->
                DiscussionService.getOrCreateChat(
                    viewModel.coach.value!!,
                    viewModel.coachClub.value!!,
                    other,
                    club
                )
            }
        }

        binding.senderTextBox.textChangedListener {
            beforeTextChanged { charSequence, i, i2, i3 ->
                viewModel.typeStart()
            }

            onTextChanged { charSequence, i, i2, i3 ->
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
                viewModel.typeStop()
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

        binding.imageSenderButton.setOnClickListener {
            viewModel.other.observe(viewLifecycleOwner) { other ->
                DiscussionService.sendChatMessage(
                    viewModel.coach.value!!,
                    viewModel.coachClub.value!!,
                    other,
                    binding.senderTextBox.text.toString()
                )
            }
            binding.senderTextBox.setText("")
        }

        binding.fabSendImage.setOnClickListener {
            toast("Not implemented")
        }

        viewModel.other.observe(viewLifecycleOwner) { other ->
            try {
                addChatMessageListener(other.id, requireContext(), this::updateRecyclerView)
            } catch (e: Exception) {
                requireActivity().onBackPressed()
                Log.e(TAG, "Error in messageListener: $e")
                toast("Error in messageListener: $e")
            }
        }

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                shouldInitRecycler = true
                Log.i(TAG, "shouldInitRecycler = $shouldInitRecycler [Home]")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateRecyclerView(messages: List<Item>) {
        Log.i(TAG, "shouldInitRecycler = $shouldInitRecycler [RecyclerView]")
        fun init() {
            binding.recyclerViewMessages.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = GroupAdapter<GroupieViewHolder>().apply {
                    messageSection = Section(messages)
                    this.add(messageSection)
                }
            }
            shouldInitRecycler = false
        }

        fun updateItems() = messageSection.update(messages)

        if (shouldInitRecycler) init() else updateItems()

//        binding.recyclerViewMessages.scrollToPosition(recyclerViewMessages.adapter!!.itemCount - 1)
    }
}