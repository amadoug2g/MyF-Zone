package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsParticipantViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsParticipantViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentEventDetailsBinding
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.detailevent.*
import com.myfzone_sport.myf_zone.usecases.notification.GetOwnerTokenUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.util.Tracking

private const val ARG_PARAM1 = "eventId"

class EventDetailsParticipantFragment : Fragment() {

    companion object {
        private lateinit var binding: FragmentEventDetailsBinding
        private lateinit var viewModel: EventDetailsParticipantViewModel
        private lateinit var viewModelFactory: EventDetailsParticipantViewModelFactory
        private var eventId: String? = null
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TRACKING.logEvent(Tracking.EVENT_DETAILS, null)

        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }

        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        //View Model
        val getEventFromIdUseCase = GetEventFromIdUseCase(repository)
        val getImageReferenceUseCase = GetImageReferenceUseCase(repository)
        val getOwnerFromEventUseCase = GetOwnerFromEventUseCase(repository)
        val getAllParticipantsFromEventUseCase = GetAllParticipantsFromEventUseCase(repository)
        val getOwnerTokenUseCase = GetOwnerTokenUseCase(repository)
        val joinEventUseCase = JoinEventUseCase(repository)
        val leaveEventUseCase = LeaveEventUseCase(repository)

        viewModelFactory = EventDetailsParticipantViewModelFactory(
            getEventFromIdUseCase,
            getImageReferenceUseCase,
            getOwnerFromEventUseCase,
            getOwnerTokenUseCase,
            getAllParticipantsFromEventUseCase,
            joinEventUseCase,
            leaveEventUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(EventDetailsParticipantViewModel::class.java)

        viewModel.assignEventId(eventId!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_event_details, container, false
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.eventDetailDescription.justificationMode = JUSTIFICATION_MODE_INTER_WORD
        }

        setupViews()

        setupObservers()

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViews() {
        setupEvent()

        val bundle = bundleOf("eventId" to eventId)
        binding.participantList.setOnClickListener {
            navigate(
                R.id.eventDetailsToEventParticipants,
//                R.id.eventDetailsToHome,
                bundle
            )
        }
//        binding.participantList.setOnClickListener { toast("tapped")  }

        viewModel.event.observe(viewLifecycleOwner, { event ->
            val participantCount =
                "Participants (${viewModel.validParticipantCount.value}/${event.nbTeam})"
            binding.participantCount.text = participantCount
        })
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })
    }

    private fun setupEvent() {
        viewModel.event.observe(viewLifecycleOwner, { event ->
            binding.event = event
        })

        viewModel.userImagePath.observe(viewLifecycleOwner, {
            displayUserImage()
        })

        viewModel.eventOwner.observe(viewLifecycleOwner, { owner ->
            binding.owner = owner
            viewModel.getImageReference(owner.clubLogo)
        })
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }
    //endregion

    //region View Methods
    private fun loadingStart() {
//        binding.progressBar.visibility = View.VISIBLE
        binding.eventDetailsParticipantShimmerLayout.startShimmer()
        binding.eventDetailsParticipantShimmerLayout.visibility = View.VISIBLE
        binding.eventDetailsParticipantLayout.visibility = View.GONE
    }

    private fun loadingStop() {
//        binding.progressBar.visibility = View.INVISIBLE
        binding.eventDetailsParticipantShimmerLayout.stopShimmer()
        binding.eventDetailsParticipantShimmerLayout.visibility = View.GONE
        binding.eventDetailsParticipantLayout.visibility = View.VISIBLE
    }

    private fun showError(message: String? = "") {
        Snackbar.make(
            binding.eventCardDate,
            if (!message.isNullOrEmpty()) message else viewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }

    private fun showProgressBar() {
        binding.eventDetailsParticipantShimmerLayout.startShimmer()
        binding.eventDetailsParticipantShimmerLayout.visibility = View.VISIBLE
        binding.eventDetailsParticipantShimmerLayout.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.eventDetailsParticipantShimmerLayout.stopShimmer()
        binding.eventDetailsParticipantShimmerLayout.visibility = View.GONE
        binding.eventDetailsParticipantShimmerLayout.visibility = View.VISIBLE
    }

    private fun displayUserImage() {
        GlideApp.with(this).apply {
            load(viewModel.userImagePath.value)
                .centerCrop()
                .into(binding.ownerClubImage)
        }
    }
    //endregion
}