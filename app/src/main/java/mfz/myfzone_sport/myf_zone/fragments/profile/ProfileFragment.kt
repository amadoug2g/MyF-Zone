package mfz.myfzone_sport.myf_zone.fragments.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentProfileBinding
import mfz.myfzone_sport.myf_zone.fragments.profile.ProfileService.getImageReference
import mfz.myfzone_sport.myf_zone.glide.GlideApp
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.event.Event
import org.jetbrains.anko.support.v4.toast

class ProfileFragment : Fragment() {
    companion object {
        private val TAG = ProfileFragment::class.java.simpleName
        private lateinit var viewModel: ProfileViewModel
        private lateinit var binding: FragmentProfileBinding
        private lateinit var viewModelFactory: ProfileViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_profile,
            container,
            false
        )

        viewModelFactory = ProfileViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)

        binding.apply {
            lifecycleOwner = this@ProfileFragment
            executePendingBindings()
        }

        lifecycleScope.launch {
            loadUser()
            loadUserClub()
            loadUserEvents()
        }

        return binding.root
    }

    private suspend fun loadUser() {
        viewModel.getCurrentUser().collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
//                    hideProgressBar()
                    val user = state.data
                    binding.user = user
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    showToast(message)
                }
            }
        }
    }

    private suspend fun loadUserClub() {
        viewModel.getCurrentUserClub().collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
//                    hideProgressBar()
                    val club = state.data
                    binding.club = club
                    Log.d(TAG, "club: ${club.clubLogo}")
                    binding.profileSubCategory.text =
                        if (club.subCategoryName.isNullOrEmpty()) "" else "- ${club.subCategoryName}"

                    GlideApp.with(this).apply {
                        load(getImageReference(club.clubLogo))
                            .centerCrop()
                            .into(binding.profileClubImage)
                    }
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    showToast(message)
                }
            }
        }
    }

    private suspend fun loadUserEvents() {
        viewModel.getCurrentUserEvents().collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    val eventList = state.data
                    eventList.forEach { event -> loadEventOwner(event.id, eventList) }
                    hideProgressBar()
                    setupEventRecyclerView(eventList)
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    showToast(message)
                }
            }
        }
    }

    private suspend fun loadEventOwner(eventId: String, eventList: MutableList<Event>) {
        viewModel.getEventOwner(eventId).collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
//                    hideProgressBar()
                    val eventOwner = state.data
                    eventList.forEach { event -> event.owner = eventOwner }
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    showToast(message)
                }
            }
        }
    }

    private fun setupEventRecyclerView(eventList: MutableList<Event>) {
        val adapter = ProfileEventAdapter()
        adapter.submitList(eventList)
        binding.profileEventRecycler.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
        }
    }

    private fun showProgressBar() {
//        binding.profileProgressBar.apply {
//            visibility = View.VISIBLE
//        }

        binding.profileShimmerLayout.startShimmer()
        binding.profileShimmerLayout.visibility = View.VISIBLE
        binding.profileLayout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        binding.profileShimmerLayout.startShimmer()
    }

    override fun onStop() {
        super.onStop()
        binding.profileShimmerLayout.stopShimmer()
    }

    private fun hideProgressBar() {
//        binding.profileProgressBar.apply {
//            visibility = View.GONE
//        }
        binding.profileShimmerLayout.stopShimmer()
        binding.profileShimmerLayout.visibility = View.GONE
        binding.profileLayout.visibility = View.VISIBLE
    }

    private fun showToast(string: String) {
        toast(string)
    }
}