package mfz.myfzone_sport.myf_zone.fragments.discussion_page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentDiscussionBinding
import org.jetbrains.anko.support.v4.toast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "coachId"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DiscussionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiscussionFragment : Fragment() {
    companion object {
        private val TAG = DiscussionFragment::class.java.simpleName
        private var coachId: String? = null
        private var param2: String? = null

        private lateinit var binding: FragmentDiscussionBinding
        private lateinit var viewModel: DiscussionViewModel
        private lateinit var messagesListenerRegistration: ListenerRegistration
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

//        viewModel.textMessage.observe(viewLifecycleOwner) { text ->
//            Log.i(TAG,"monitoring $text")
//            if (text.isNullOrEmpty()) {
//                binding.imageSenderButton.isEnabled = false
//                Log.i(TAG,"message is $text")
//            } else {
//                binding.imageSenderButton.isEnabled = true
//                Log.i(TAG,"message is $text")
//            }
//        }

        viewModel.other.observe(viewLifecycleOwner) { other ->
            //Page Title
            (activity as AppCompatActivity).supportActionBar?.apply {
                title = other.firstName
            }

            viewModel.otherClub.observe(viewLifecycleOwner) { club ->
                DiscussionService.getOrCreateChatChannel(
                    viewModel.coach.value!!,
                    viewModel.coachClub.value!!,
                    other,
                    club
                )
            }
        }

        binding.senderTextBox.doAfterTextChanged {
            when (it.toString().isNullOrEmpty()) {
                true -> {
                    binding.imageSenderButton.isEnabled = false
                }
                false -> {
                    binding.imageSenderButton.isEnabled = true
                }
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
            toast("${binding.senderTextBox.text}")
            binding.senderTextBox.setText("")
        }

        return binding.root
    }
}