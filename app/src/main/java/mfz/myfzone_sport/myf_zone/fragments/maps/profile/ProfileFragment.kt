package mfz.myfzone_sport.myf_zone.fragments.maps.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentProfileBinding
import mfz.myfzone_sport.myf_zone.fragments.maps.profile.ProfileService.getImageReference
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
                    showProgressBar(profileProgressBar)
                }
                is State.Success -> {
                    hideProgressBar(profileProgressBar)
                    val user = state.data
                    binding.user = user
                }
                is State.Failed -> {
                    hideProgressBar(profileProgressBar)
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
                    showProgressBar(profileProgressBar)
                }
                is State.Success -> {
                    hideProgressBar(profileProgressBar)
                    val club = state.data
                    binding.club = club

                    //TODO: move getImageReference to ProfileViewModel
                    GlideApp.with(this).apply {
                        load(getImageReference(club.clubLogo))
                            .centerCrop()
                            .into(binding.profileClubImage)
                    }
                }
                is State.Failed -> {
                    hideProgressBar(profileProgressBar)
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
                    showProgressBar(profileProgressBar)
                }
                is State.Success -> {
                    hideProgressBar(profileProgressBar)
                    val eventList = state.data
                    eventList.forEach { event -> loadEventOwner(event.id, eventList) }
                    setupEventRecyclerView(eventList)
                }
                is State.Failed -> {
                    hideProgressBar(profileProgressBar)
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
                    showProgressBar(profileProgressBar)
                }
                is State.Success -> {
                    hideProgressBar(profileProgressBar)
                    val eventOwner = state.data
                    eventList.forEach { event -> event.owner = eventOwner }
                }
                is State.Failed -> {
                    hideProgressBar(profileProgressBar)
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

    private fun showProgressBar(progressBar: ProgressBar) {
        CoroutineScope(Main).launch {
            progressBar.apply {
                visibility = View.VISIBLE
            }
        }
    }

    private fun hideProgressBar(progressBar: ProgressBar) {
        CoroutineScope(Main).launch {
            progressBar.apply {
                visibility = View.GONE
            }
        }
    }

    private fun showToast(string: String) {
        toast(string)
    }
}