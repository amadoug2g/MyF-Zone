package com.myfzone_sport.myf_zone.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.Query
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.CardEventProfileBinding
import com.myfzone_sport.myf_zone.databinding.FragmentProfileBinding
import com.myfzone_sport.myf_zone.fragments.profile.ProfileService.getImageReference
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.coroutines.flow.collect
import org.jetbrains.anko.support.v4.toast

class ProfileFragment : Fragment() {
    companion object {
        private val TAG = ProfileFragment::class.java.simpleName
        private lateinit var viewModel: ProfileViewModel
        private lateinit var binding: FragmentProfileBinding
        private lateinit var viewModelFactory: ProfileViewModelFactory
        private var adapter: FirestoreRecyclerAdapter<Event, EventHolder>? = null
    }

    class EventHolder(val binding: CardEventProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            with(binding) {
                binding.event = event

                binding.cardViewDetailProfile.setOnClickListener {
                    navigate(event)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): EventHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    CardEventProfileBinding.inflate(layoutInflater, parent, false)
                return EventHolder(binding)
            }
        }

        private fun navigate(event: Event) {
            TRACKING.logEvent(Tracking.ACCOUNT_OPEN_EVENT, null)
            val bundle = bundleOf("eventId" to event.id)
            navigate(R.id.profileToEventDetailsOwner, bundle)
        }

        private fun navigate(destination: Int, extra: Bundle? = null) {
            Navigation
                .findNavController(itemView)
                .navigate(destination, extra)
        }
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModelFactory = ProfileViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)

        val recyclerOptions = FirestoreRecyclerOptions.Builder<Event>()
            .setQuery(
                viewModel.getQuery().orderBy("date", Query.Direction.DESCENDING),
                Event::class.java
            )
            .setLifecycleOwner(this)
            .build()

        adapter = object :
            FirestoreRecyclerAdapter<Event, EventHolder>(recyclerOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
                return EventHolder.from(parent)
            }

            override fun onBindViewHolder(
                holder: EventHolder,
                position: Int,
                model: Event
            ) {
                holder.bind(model)
//                if (itemCount > 0)
//                    binding.count = itemCount
//                else
//                    binding.count = 0
            }

            override fun onDataChanged() {
                binding.count = itemCount

                val params = binding.profileEventRecycler.layoutParams
                params.height = 320 * itemCount
                binding.profileEventRecycler.layoutParams = params
            }
        }
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

        binding.apply {
            lifecycleOwner = this@ProfileFragment
            executePendingBindings()
            setupRecyclerParameters()
        }

        binding.user = ManagerAuth.activeCoach
        binding.club = ManagerAuth.activeCoachClubAffiliation

        loadUser()

        binding.profileSettings.setOnClickListener {
            TRACKING.logEvent(Tracking.SETTINGS, null)
            navigate(R.id.profileToSettings)
        }

        binding.profileAffiliation.setOnClickListener {
            navigate(R.id.profileToAffiliation)
//            affiliationChoiceWindow()
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
    private fun loadUser() {
        GlideApp.with(this).apply {
            load(getImageReference(ManagerAuth.activeCoachClubAffiliation!!.clubLogo))
                .centerCrop()
                .into(binding.profileClubImage)
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
                    binding.count = eventList.size
//                    eventList.forEach { event -> loadEventOwner(event.id, eventList) }
                    hideProgressBar()
//                    setupEventRecyclerView(eventList)
//                    setupRecyclerParameters()
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
        val titleNewAffiliation = getString(R.string.title_new_affiliation)
        val titleAffiliationList = getString(R.string.title_affiliation_list)
        val titleClubList = getString(R.string.affiliation_club_list)
        val items = arrayOf(titleNewAffiliation, titleAffiliationList, titleClubList)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.membership))
            .setItems(items) { _, selected ->
                // Respond to item chosen
                when (selected) {
                    0 -> {
                        navigate(R.id.profileToAffiliation)
                    }
                    1 -> {
                        toast("Selected $titleAffiliationList")
                    }
                    2 -> {
                        navigate(R.id.profileToAffiliationClubList)
                    }
                }
            }
            .show()


//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle(getString(R.string.affiliate))
//            .setMessage(getString(R.string.enter_event_msg))
//            .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
////                confirmParticipation()
//            }
//            .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
//            }.show()
    }
    //endregion

    //region RecyclerView
    private fun setupRecyclerParameters() {
        binding.profileEventRecycler.setHasFixedSize(false)
        binding.profileEventRecycler.layoutManager =
            LinearLayoutManager(requireContext())
        binding.profileEventRecycler.adapter = adapter
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