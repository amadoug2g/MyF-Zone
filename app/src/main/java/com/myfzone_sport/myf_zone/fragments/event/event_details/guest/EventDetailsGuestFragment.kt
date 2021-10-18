package com.myfzone_sport.myf_zone.fragments.event.event_details.guest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.CardEventParticipantGuestBinding
import com.myfzone_sport.myf_zone.databinding.FragmentEventDetailsGuestBinding
import com.myfzone_sport.myf_zone.fragments.event.event_details.guest.EventDetailsGuestService.getImageReference
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.android.synthetic.main.event_detail_cardview_map.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.toast
import java.util.*

private const val ARG_PARAM1 = "eventId"

class EventDetailsGuestFragment : Fragment() {
    companion object {
        private val TAG = this::class.java.simpleName
        private var eventId: String? = null
        private var adapter: FirestoreRecyclerAdapter<EventParticipant, ParticipantHolder>? =
            null

        private lateinit var binding: FragmentEventDetailsGuestBinding
        private lateinit var viewModel: EventDetailsGuestViewModel
    }

    class ParticipantHolder(val binding: CardEventParticipantGuestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: EventParticipant) {
            with(binding) {
                binding.participant = participant

                try {
                    GlideApp.with(itemView).apply {
                        load(getImageReference(participant.clubLogo))
                            .placeholder(R.drawable.ic_account)
                            .centerCrop()
                            .into(binding.eventDetailParticipantImage)
                    }
                } catch (e: Exception) {
                    Log.e("$TAG ParticipantHolder", "Image could not load: $e")
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ParticipantHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    CardEventParticipantGuestBinding.inflate(layoutInflater, parent, false)
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

        viewModel = ViewModelProvider(this).get(EventDetailsGuestViewModel::class.java)

        viewModel.eventId.value = eventId!!

        val recyclerOptions = FirestoreRecyclerOptions.Builder<EventParticipant>()
            .setQuery(
                viewModel.getQuery(eventId!!).whereEqualTo("status", "validate"),
                EventParticipant::class.java
            )
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

                val params = binding.eventDetailGuestParticipantList.layoutParams
                params.height = 320 * itemCount
                binding.eventDetailGuestParticipantList.layoutParams = params
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_event_details_guest,
            container,
            false
        )

        lifecycleScope.launch {
            assignOwner()
            assignEvent(savedInstanceState)
        }

        binding.apply {
            lifecycleOwner = this@EventDetailsGuestFragment
            setupRecyclerParameters()
            executePendingBindings()
        }

        if (ManagerAuth.isConnected) {
            if (!ManagerAuth.isAffiliated) {
                binding.userInfoCardStatus.messageChatListNotSignedIn.visibility = View.GONE
                binding.userInfoCardStatus.messageChatListNotAffiliated.visibility = View.VISIBLE
            }
        } else {
            binding.userInfoCardStatus.messageChatListNotSignedIn.visibility = View.VISIBLE
            binding.userInfoCardStatus.messageChatListNotAffiliated.visibility = View.GONE
        }

        binding.userInfoCardStatus.messageChatListNotAffiliated.setOnClickListener { navigate(R.id.eventDetailsToAffiliationRequest) }
        binding.userInfoCardStatus.messageChatListNotSignedIn.setOnClickListener { navigate(R.id.eventDetailsToSignUp) }

        return binding.root
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
                    val message = "Erreur: ${state.message}"
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

                    GlideApp.with(this).apply {
                        load(getImageReference(owner.clubLogo))
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
    //endregion

    //region Loading
    private fun showProgressBar() {
        binding.eventDetailGuestShimmerLayout.startShimmer()
        binding.eventDetailGuestShimmerLayout.visibility = View.VISIBLE
        binding.eventDetailGuestLayout.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.eventDetailGuestShimmerLayout.stopShimmer()
        binding.eventDetailGuestShimmerLayout.visibility = View.GONE
        binding.eventDetailGuestLayout.visibility = View.VISIBLE
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
        binding.eventDetailGuestParticipantList.setHasFixedSize(false)
        binding.eventDetailGuestParticipantList.layoutManager =
            LinearLayoutManager(requireContext())
        binding.eventDetailGuestParticipantList.adapter = adapter
        binding.eventDetailGuestParticipantList.isNestedScrollingEnabled = false
    }
    //endregion RecyclerView
}