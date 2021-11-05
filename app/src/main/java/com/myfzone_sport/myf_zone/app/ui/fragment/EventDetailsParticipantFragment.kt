package com.myfzone_sport.myf_zone.app.ui.fragment

import android.content.DialogInterface
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.adapter.ParticipantsPreviewAdapter
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsParticipantViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsParticipantViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentEventDetailsBinding
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.detailevent.*
import com.myfzone_sport.myf_zone.usecases.notification.GetOwnerTokenUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.util.Tracking
import java.util.*

private const val ARG_PARAM1 = "eventId"

class EventDetailsParticipantFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentEventDetailsBinding
        private lateinit var viewModel: EventDetailsParticipantViewModel
        private lateinit var viewModelFactory: EventDetailsParticipantViewModelFactory
        private lateinit var participantPreviewAdapter: ParticipantsPreviewAdapter
        private var eventId: String? = null
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TRACKING.logEvent(Tracking.EVENT_DETAILS, null)

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
            inflater, R.layout.fragment_event_details, container, false
        )

        setupViews(savedInstanceState)
        setupObservers()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.eventDetailsParticipantShimmerLayout.startShimmer()
    }

    override fun onStop() {
        super.onStop()
        binding.eventDetailsParticipantShimmerLayout.stopShimmer()
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
    }

    private fun setupViews(savedInstanceState: Bundle?) {
        binding.backArrow.apply {
            background = null
            setOnClickListener { requireActivity().onBackPressed() }
        }

        binding.eventDetailParticipateBtn.setOnClickListener {
            joinEventDialog()
        }

        binding.eventDetailLeaveBtn.setOnClickListener {
            leaveEventDialog()
        }

        binding.eventDetailContactLayout.setOnClickListener {
            navigate(R.id.eventDetailsToMessageFragment)
        }

        binding.participantRecyclerview.suppressLayout(true)

        setupEvent(savedInstanceState)
        setUpParticipantRecycler()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.eventDetailDescription.justificationMode = JUSTIFICATION_MODE_INTER_WORD
        }

        val bundle = bundleOf("eventId" to eventId)

        binding.participantRecyclerview.setOnClickListener {
            navigate(R.id.eventDetailsToEventParticipants, bundle)
        }

        binding.eventDetailShareBtn.setOnClickListener {
            navigate(R.id.eventDetailsToEventParticipants, bundle)
        }
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })

        viewModel.isUserParticipating.observe(viewLifecycleOwner, {
            if (it) {
                binding.eventDetailParticipateBtn.visibility = View.GONE
                binding.eventDetailLeaveBtn.visibility = View.VISIBLE
            } else {
                binding.eventDetailParticipateBtn.visibility = View.VISIBLE
                binding.eventDetailLeaveBtn.visibility = View.GONE
            }
        })

        viewModel
    }
    //endregion

    //region Event
    private fun setupEvent(savedInstanceState: Bundle?) {
        viewModel.event.observe(viewLifecycleOwner, { event ->
            binding.event = event
            mapPreview(event, savedInstanceState)
        })

        viewModel.userImagePath.observe(viewLifecycleOwner, {
            displayUserImage()
        })

        viewModel.eventOwner.observe(viewLifecycleOwner, { owner ->
            binding.owner = owner
            viewModel.getImageReference(owner.clubLogo)
        })
    }

    private fun joinEventDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.participation_title))
            .setMessage(getString(R.string.enter_event_msg))
            .setNeutralButton(getString(R.string.cancel_message)) { _: DialogInterface, _: Int -> }
            .setPositiveButton(getString(R.string.participation_accept)) { _: DialogInterface, _: Int ->
                val participant = EventParticipant()

                viewModel.joinEvent(eventId!!, participant.confirm())
            }
            .show()
    }

    private fun leaveEventDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.participation_title))
            .setMessage(getString(R.string.exit_event_msg))
            .setNeutralButton(getString(R.string.cancel_message)) { _: DialogInterface, _: Int -> }
            .setPositiveButton(getString(R.string.exit_text)) { _: DialogInterface, _: Int ->
                viewModel.leaveEvent(eventId!!)
            }
            .show()
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

    //region RecyclerView
    private fun setUpParticipantRecycler() {
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getImageReferenceUseCase = GetImageReferenceUseCase(repository)

        participantPreviewAdapter = ParticipantsPreviewAdapter(getImageReferenceUseCase, eventId!!)
        binding.participantRecyclerview.adapter = participantPreviewAdapter
        binding.participantRecyclerview.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//        binding.participantRecyclerview.setChildDrawingOrderCallback(BackwardsDrawingOrderCallback())

        viewModel.eventParticipantsValid.observe(viewLifecycleOwner, {
            participantPreviewAdapter.setData(it)
            binding.validCount = it.size
        })
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
        binding.eventDetailsParticipantShimmerLayout.startShimmer()
        binding.eventDetailsParticipantShimmerLayout.visibility = View.VISIBLE
        binding.eventDetailsParticipantLayout.visibility = View.GONE
    }

    private fun loadingStop() {
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

    class BackwardsDrawingOrderCallback : RecyclerView.ChildDrawingOrderCallback {
        override fun onGetChildDrawingOrder(childCount: Int, i: Int) = childCount - i - 1
    }
}