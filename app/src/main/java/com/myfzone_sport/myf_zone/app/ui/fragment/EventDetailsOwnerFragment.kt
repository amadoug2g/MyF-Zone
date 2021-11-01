package com.myfzone_sport.myf_zone.app.ui.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD
import android.net.Uri
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsOwnerViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsOwnerViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentEventDetailsOwner2Binding
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetEventFromIdUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetOwnerFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.util.Tracking
import java.util.*

private const val ARG_PARAM1 = "eventId"

class EventDetailsOwnerFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentEventDetailsOwner2Binding
        private lateinit var viewModel: EventDetailsOwnerViewModel
        private lateinit var viewModelFactory: EventDetailsOwnerViewModelFactory
        private var eventId: String? = null
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        TRACKING.logEvent(Tracking.EVENT_DETAILS, null)
        super.onCreate(savedInstanceState)

        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }

        setupViewModel()

        viewModel.assignEventId(eventId!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_event_details_owner2,
            container,
            false
        )

        setupViews(savedInstanceState)

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

        viewModelFactory = EventDetailsOwnerViewModelFactory(
            getEventFromIdUseCase,
            getImageReferenceUseCase,
            getOwnerFromEventUseCase,
            getAllParticipantsFromEventUseCase,
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(EventDetailsOwnerViewModel::class.java)
    }

    private fun setupViews(savedInstanceState: Bundle?) {
        binding.backArrow.background = null

        binding.backArrow.setOnClickListener {
            requireActivity().onBackPressed()
        }

        setupEvent(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.eventDetailDescription.justificationMode = JUSTIFICATION_MODE_INTER_WORD
        }

        val bundle = bundleOf("eventId" to eventId)
        binding.participantList.setOnClickListener {
            navigate(R.id.eventDetailsOwnerToEventParticipantsOwner, bundle)
        }
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })
    }
    //endregion

    //region Event
    private fun setupEvent(savedInstanceState: Bundle?) {
        viewModel.event.observe(viewLifecycleOwner, { event ->
            binding.event = event
            mapPreview(event, savedInstanceState)

            val participantCount =
                "Participants (${viewModel.validParticipantCount.value}/${event.nbTeam})"
            binding.participantCount.text = participantCount
        })

        viewModel.userImagePath.observe(viewLifecycleOwner, {
            displayUserImage()
        })

        viewModel.eventOwner.observe(viewLifecycleOwner, { owner ->
            binding.owner = owner
            viewModel.getImageReference(owner.clubLogo)
        })
    }

    private fun mapPreview(event: Event, savedInstanceState: Bundle?) {
        binding.eventDetailMap.eventDetailMap.onCreate(savedInstanceState)
        binding.eventDetailMap.eventDetailMap.onResume()

        MapsInitializer.initialize(requireActivity().applicationContext)

        binding.eventDetailMap.eventDetailMap.getMapAsync { map ->
            val markerOptions = MarkerOptions().apply {
                position(event.getPosition())
                snippet(event.address)
            }

            map.apply {
                uiSettings.apply {
                    setAllGesturesEnabled(false)
                    isZoomControlsEnabled = false
                    isRotateGesturesEnabled = false
                    isScrollGesturesEnabled = false
                    isScrollGesturesEnabledDuringRotateOrZoom = false
                    isZoomControlsEnabled = false
                    isTiltGesturesEnabled = false
                }

                setPadding(0, 0, 0, 40)
                addMarker(markerOptions)
                moveCamera(CameraUpdateFactory.newLatLngZoom(event.getPosition(), 14f))
                setOnMapClickListener { redirectToMap(event.address) }
                setOnMarkerClickListener { true }
            }
        }
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }

    private fun redirectToMap(position: String) {
        val uri =
            java.lang.String.format(
                Locale.FRANCE,
                "geo:0,0?q=$position"
            )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        requireContext().startActivity(intent)
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

    private fun displayUserImage() {
        GlideApp.with(this).apply {
            load(viewModel.userImagePath.value)
                .centerCrop()
                .into(binding.ownerClubImage)
        }
    }
    //endregion
}