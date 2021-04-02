package com.myfzone_sport.myf_zone.fragments.event.event_details.participant

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.CardEventParticipantBinding
import com.myfzone_sport.myf_zone.databinding.FragmentEventDetailsParticipantBinding
import com.myfzone_sport.myf_zone.fragments.event.event_details.EventDetailsService
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.chat.MessagingService
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.model.event.EventParticipant
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.android.synthetic.main.event_detail_cardview_map.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.toast
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "eventId"

class EventDetailsParticipantFragment : Fragment() {
    companion object {
        private val TAG = this::class.java.simpleName
        private var eventId: String? = null
        private var adapter: FirestoreRecyclerAdapter<EventParticipant, ParticipantHolder>? = null

        private lateinit var binding: FragmentEventDetailsParticipantBinding
        private lateinit var viewModel: EventDetailsParticipantViewModel
    }

    class ParticipantHolder(val binding: CardEventParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: EventParticipant) {
            with(binding) {
                binding.participant = participant

                try {
                    GlideApp.with(itemView).apply {
                        load(EventDetailsService.getImageReference(participant.clubLogo))
                            .placeholder(R.drawable.ic_account)
                            .centerCrop()
                            .into(binding.eventDetailParticipantImage)
                    }
                } catch (e: Exception) {
                    Log.e("ParticipantHolder", "Image could not load: $e")
                }

                try {
                    var dotBg: Int = R.drawable.notification_dot_blue
                    when (participant.status) {
                        "pending" -> dotBg = R.drawable.notification_dot_blue
                        "validate" -> dotBg = R.drawable.notification_dot_green
                        "refused" -> dotBg = R.drawable.notification_dot_red
                    }
                    binding.notificationDotOwner.setImageResource(dotBg)
                } catch (e: Exception) {
                    Log.e("ParticipantHolder", "Notification Dot could not load: $e")
                }

                viewModel.checkIsUserParticipant(participant)

                if (viewModel.isUserParticipant.value!!) {
                    binding.notificationDotOwner.visibility = View.VISIBLE
                } else {
                    binding.notificationDotOwner.visibility = View.GONE
                }

                binding.cancelParticipation.visibility =
                    if (viewModel.isUserParticipant.value!!) View.VISIBLE else View.GONE

                binding.cancelParticipation.setOnClickListener {
                    TRACKING.logEvent(Tracking.EVENT_DETAILS_COACH_CANCEL_PARTICIPATION, null)
                    cancelParticipation()
                }
            }
        }

        private fun cancelParticipation() {
            MaterialAlertDialogBuilder(itemView.context)
                .setTitle(itemView.context.getString(R.string.event_exit))
                .setMessage(itemView.context.getString(R.string.exit_event_msg))
                .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                    viewModel.removeParticipant()
                    EventDetailsParticipantFragment.binding.participateButton.visibility =
                        View.VISIBLE
                }
                .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
                }.show()
        }

        companion object {
            fun from(parent: ViewGroup): ParticipantHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CardEventParticipantBinding.inflate(layoutInflater, parent, false)
                return ParticipantHolder(binding)
            }
        }
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TRACKING.logEvent(Tracking.EVENT_DETAILS, null)

        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }

        viewModel = ViewModelProvider(this).get(EventDetailsParticipantViewModel::class.java)

        viewModel.eventId.value = eventId!!

        lifecycleScope.launch {
            viewModel.assignEvent()
            viewModel.assignOwner()
            viewModel.checkParticipationStatus()
        }

        val recyclerOptions = FirestoreRecyclerOptions.Builder<EventParticipant>()
            .setQuery(viewModel.getQuery(eventId!!), EventParticipant::class.java)
            .setLifecycleOwner(this)
            .build()

        adapter = object :
            FirestoreRecyclerAdapter<EventParticipant, ParticipantHolder>(recyclerOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantHolder {
                return ParticipantHolder.from(parent)
            }

            override fun onBindViewHolder(
                holder: ParticipantHolder,
                position: Int,
                model: EventParticipant
            ) {
                holder.bind(model)
            }

            override fun onDataChanged() {

                binding.eventDetailEmptyList.visibility =
                    if (itemCount == 0) View.VISIBLE else View.GONE

                val params = binding.eventDetailParticipantList.layoutParams
                params.height = 320 * itemCount
                binding.eventDetailParticipantList.layoutParams = params

                try {
                    lifecycleScope.launch {
                        val count =
                            EventDetailsService.getValidParticipantCount(viewModel.eventId.value!!)
                        try {
                            binding.eventDetailNbTeam.text = count
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in [onDataChanged]: $e")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error [onDataChanged] participant count: $e")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_event_details_participant,
            container,
            false
        )

        lifecycleScope.launch {
            assignOwner()
            assignEvent(savedInstanceState)
            viewModel.assignParticipants()
            viewModel.checkParticipationStatus()
        }

        viewModel.owner.observe(viewLifecycleOwner) { owner ->
            lifecycleScope.launch {
                viewModel.assignOwnerToken(owner.coachId)
            }
        }

        binding.apply {
            lifecycleOwner = this@EventDetailsParticipantFragment
            setupRecyclerParameters()
            executePendingBindings()
        }

        binding.participateButton.setOnClickListener { participationWindow() }

        binding.cardEventOwner.ownerMessageIcon.setOnClickListener {
            TRACKING.logEvent(Tracking.EVENT_DETAILS_OPEN_CHAT, null)
            userConversation()
        }

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

    //region Event Details
    private suspend fun assignEvent(savedInstanceState: Bundle?) {
        viewModel.getEvent(viewModel.eventId.value!!).collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    val event = state.data
                    binding.event = event
                    binding.eventDetailCardviewLayout.eventDetailType.text = eventType(event)

                    //Page Title
                    (activity as AppCompatActivity).supportActionBar?.apply {
                        title = eventType(event)
                    }

                    //Event Map
                    mapView(event, savedInstanceState)

                    hideProgressBar()
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    showToast(message)
                }
            }
        }
    }

    private suspend fun assignOwner() {
        viewModel.getOwnerFromEvent(viewModel.eventId.value!!).collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    val owner = state.data
                    binding.owner = owner
                    Log.i(TAG, "Owner URL: ${owner.clubLogo}")
                    GlideApp.with(this).apply {
                        load(EventDetailsService.getImageReference(owner.clubLogo))
                            .centerCrop()
                            .into(binding.cardEventOwner.eventDetailOwnerImage)
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

    private fun mapView(event: Event, savedInstanceState: Bundle?) {
        event_detail_map.onCreate(savedInstanceState)
        event_detail_map.onResume()

        try {
            MapsInitializer.initialize(context)
        } catch (e: Exception) {
            Log.d(TAG, "Error in mapView: $e")
        }

        event_detail_map.getMapAsync { map ->
            val markerOptions = MarkerOptions().apply {
                position(event.getPosition())
                snippet(event.address)
            }

            map.uiSettings.apply {
                setAllGesturesEnabled(false)
                isZoomControlsEnabled = false
                isRotateGesturesEnabled = false
                isScrollGesturesEnabled = false
                isScrollGesturesEnabledDuringRotateOrZoom = false
                isZoomControlsEnabled = false
                isTiltGesturesEnabled = false
            }
            map.addMarker(markerOptions)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(event.getPosition(), 14f))
            map.setOnMapClickListener {
                redirectToMap(event.address)
            }
            map.setOnMarkerClickListener {
                true
            }
        }
    }

    private fun eventType(event: Event): String {
        return when (event.type) {
            "friendly" -> {
                getString(R.string.friendly_event)
            }
            "tournament" -> {
                getString(R.string.tournament_event)
            }
            else -> {
                getString(R.string.plateau_event)
            }
        }
    }
    //endregion

    //region Coach Actions
    private fun participationWindow() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.enter_event))
            .setMessage(getString(R.string.enter_event_msg))
            .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                confirmParticipation()
            }
            .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
            }.show()
    }

    private fun confirmParticipation() {
        val participant = EventParticipant()

        lifecycleScope.launch {
            TRACKING.logEvent(Tracking.EVENT_DETAILS_COACH_PARTICIPATION, null)
            addParticipant(participant.confirm())
            binding.participateButton.visibility = View.GONE
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            viewModel.owner.observe(viewLifecycleOwner) { owner ->
                MessagingService.eventParticipation(event, owner)
            }
        }
    }

    private fun addParticipant(participant: EventParticipant) {
        viewModel.addParticipant(participant)
    }
    //endregion Coach Actions

    //region Navigation
    private fun redirectToMap(position: String) {
        val uri =
            java.lang.String.format(
                Locale.FRANCE,
                "geo:0,0?q=$position"
            )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        requireContext().startActivity(intent)
    }

    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }

    private fun userConversation() {
        viewModel.owner.observe(viewLifecycleOwner) { owner ->
            val bundle = bundleOf("coachId" to owner.coachId)
            navigate(R.id.eventDetailsToDiscussion, bundle)
        }
    }
    //endregion

    //region Loading
    private fun showProgressBar() {
        binding.eventDetailsParticipantShimmerLayout.startShimmer()
        binding.eventDetailsParticipantShimmerLayout.visibility = View.VISIBLE
        binding.eventDetailsParticipantLayout.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.eventDetailsParticipantShimmerLayout.stopShimmer()
        binding.eventDetailsParticipantShimmerLayout.visibility = View.GONE
        binding.eventDetailsParticipantLayout.visibility = View.VISIBLE
    }
    //endregion

    //region View Methods
    private fun showToast(string: String) {
        toast(string)
    }

    fun View.snack(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        val msg = Snackbar.make(this, message, duration)
        msg.show()
    }
    //endregion

    //region RecyclerView
    private fun setupRecyclerParameters() {
        binding.eventDetailParticipantList.setHasFixedSize(false)
        binding.eventDetailParticipantList.layoutManager = LinearLayoutManager(requireContext())
        binding.eventDetailParticipantList.adapter = adapter
        binding.eventDetailParticipantList.isNestedScrollingEnabled = false
    }
    //endregion RecyclerView
}