package com.myfzone_sport.myf_zone.fragments.profile

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentProfileBinding
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.toast

class ProfileFragment : Fragment() {
    companion object {
        private val TAG = ProfileFragment::class.java.simpleName
        private lateinit var viewModel: ProfileViewModel
        private lateinit var binding: FragmentProfileBinding
        private lateinit var viewModelFactory: ProfileViewModelFactory
    }

    //region Override Methods
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

        binding.user = ManagerAuth.activeCoach
        binding.club = ManagerAuth.activeCoachClubAffiliation

        lifecycleScope.launch {
            loadUserEvents()
        }

        binding.profileSettings.setOnClickListener {
            TRACKING.logEvent(Tracking.SETTINGS, null)
            navigate(R.id.profileToSettings)
        }

        binding.profileAffiliation.setOnClickListener {
            navigate(R.id.profileToAffiliation)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.profileShimmerLayout.startShimmer()
    }

    override fun onStop() {
        super.onStop()
        binding.profileShimmerLayout.stopShimmer()
    }
    //endregion

    //region User Info
    private suspend fun loadUserEvents() {
        viewModel.getCurrentUserEvents().collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    val eventList = state.data
                    binding.count = eventList.size
//                    eventList.forEach { event -> loadEventOwner(event.id, eventList) }
                    hideProgressBar()
                    setupEventRecyclerView(eventList)
                }
                is State.Failed -> {
                    val bundleTracking = bundleOf("Profile Error [loadUserEvents]" to state.message)
                    TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                    hideProgressBar()
                    val message = "An error occurred [in loadUserEvents]: ${state.message}"
                    showToast(message)
                }
            }
        }
    }
    //endregion

    //region Affiliation
    private fun affiliationChoiceWindow() {
        val items = arrayOf("Item 1", "Item 2", "Item 3")
        //Titre - Affiliations
        //Nouvel affiliation
        //Statut des demandes

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.affiliate))
            .setItems(items) { _, _ ->
                // Respond to item chosen
            }
            .show()


        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.affiliate))
            .setMessage(getString(R.string.enter_event_msg))
            .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
//                confirmParticipation()
            }
            .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
            }.show()
    }
    //endregion

    //region RecyclerView
    private fun setupEventRecyclerView(eventList: MutableList<Event>) {
        val adapter = ProfileEventAdapter()
        adapter.submitList(eventList)
        binding.profileEventRecycler.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
        }
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }
    //endregion

    //region Loading
    private fun showProgressBar() {
        binding.profileShimmerLayout.startShimmer()
        binding.profileShimmerLayout.visibility = View.VISIBLE
        binding.profileLayout.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.profileShimmerLayout.stopShimmer()
        binding.profileShimmerLayout.visibility = View.GONE
        binding.profileLayout.visibility = View.VISIBLE
    }
    //endregion

    //region View Methods
    private fun showToast(string: String) {
        toast(string)
    }
    //endregion
}