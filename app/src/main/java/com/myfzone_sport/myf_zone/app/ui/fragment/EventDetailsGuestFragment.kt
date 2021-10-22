package com.myfzone_sport.myf_zone.app.ui.fragment

import android.database.DatabaseUtils
import android.graphics.Color
import android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsGuestViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsGuestViewModelFactory
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsParticipantViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsParticipantViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentEventDetailsGuest2Binding
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetEventFromIdUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetOwnerFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase

private const val ARG_PARAM1 = "eventId"

class EventDetailsGuestFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentEventDetailsGuest2Binding
        private lateinit var viewModel: EventDetailsGuestViewModel
        private lateinit var viewModelFactory: EventDetailsGuestViewModelFactory
        private var eventId: String? = null
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_event_details_guest2,
            container,
            false
        )

        setupViewModel()
        setupViews()
        setupObservers()

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViewModel() {
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getEventFromIdUseCase = GetEventFromIdUseCase(repository)
        val getImageReferenceUseCase = GetImageReferenceUseCase(repository)
        val getOwnerFromEventUseCase = GetOwnerFromEventUseCase(repository)
        val getAllParticipantsFromEventUseCase = GetAllParticipantsFromEventUseCase(repository)

        viewModelFactory = EventDetailsGuestViewModelFactory(
            getEventFromIdUseCase,
            getImageReferenceUseCase,
            getOwnerFromEventUseCase,
            getAllParticipantsFromEventUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(EventDetailsGuestViewModel::class.java)
    }

    private fun setupViews() {
        setupEvent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.eventDetailDescription.justificationMode = JUSTIFICATION_MODE_INTER_WORD
        }

        val bundle = bundleOf("eventId" to eventId)
        binding.participantList.setOnClickListener {
            navigate(
                R.id.eventDetailsToEventParticipants,
//                R.id.eventDetailsToHome,
                bundle
            )
        }

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
        binding.eventDetailsParticipantShimmerLayout.startShimmer()
        binding.eventDetailsParticipantShimmerLayout.visibility = View.VISIBLE
        binding.eventDetailsGuestLayout.visibility = View.GONE
    }

    private fun loadingStop() {
        binding.eventDetailsParticipantShimmerLayout.stopShimmer()
        binding.eventDetailsParticipantShimmerLayout.visibility = View.GONE
        binding.eventDetailsGuestLayout.visibility = View.VISIBLE
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

    private fun displayUserImage() {
        GlideApp.with(this).apply {
            load(viewModel.userImagePath.value)
                .centerCrop()
                .into(binding.ownerClubImage)
        }
    }
    //endregion
}