package com.example.myf_zone.fragments.maps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myf_zone.R
import com.example.myf_zone.glide.GlideApp
import com.example.myf_zone.util.event.EventUtil.getEventFromId
import com.example.myf_zone.util.event.EventUtil.getOwnerFromEvent
import com.example.myf_zone.util.user.UserAccount
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.card_event_owner.*
import kotlinx.android.synthetic.main.event_detail_cardview_map.*
import kotlinx.android.synthetic.main.event_detail_cardview_title.*
import kotlinx.android.synthetic.main.fragment_event_details_friendly.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.toast
import java.text.SimpleDateFormat
import java.util.*


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "eventId"

class EventDetailsFragment : Fragment() {
    private val TAG = EventDetailsFragment::class.java.simpleName
    private var eventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details_friendly, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showProgressBar(event_detail_progressBar)
        Log.d(TAG, eventId.toString())

        try {
            val formatEventDay = SimpleDateFormat("dd MMM Y", Locale.FRANCE)
            val formatEventHour = SimpleDateFormat("HH:mm", Locale.FRANCE)
            val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
            CoroutineScope(Main).launch {
                getEventFromId(eventId!!) { event ->

                    //Event Details
                    val eventDay = formatEventDay.format(formatDate.parse(event.date.toString())!!)
                    val eventHour =
                        formatEventHour.format(formatDate.parse(event.date.toString())!!)
                    event_detail_address.text = event.address
                    event_detail_day.text = eventDay
                    event_detail_hour.text = eventHour
                    event_detail_type.text = event.eventTypeString
                    event_detail_title.text = event.title
                    event_detail_description.text = event.description
                    event_detail_title.text = event.title
                    event_detail_team.text = event.nbTeam.toString().plus(getString(R.string.teams))
                    event_detail_imageView.setImageResource(event.eventTypeImage)
                    val teamCpt = "1/" + event.nbTeam.toString()
                    event_detail_nbTeam.text = teamCpt

                    //Event Owner
                    CoroutineScope(Main).launch {
                        ownerFields(event.id)
                    }

                    //Page Title
                    (activity as AppCompatActivity).supportActionBar?.apply {
                        title = event.eventTypeString
                    }

                    //MapView
                    event_detail_map.onCreate(savedInstanceState)
                    event_detail_map.onResume()

                    try {
                        MapsInitializer.initialize(context)
                    } catch (e: Exception) {
                        Log.d(TAG, "Error: $e")
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
//                            event_detail_map.isClickable = false
                            showMap(event.address)
                        }
                        map.setOnMarkerClickListener {
                            true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: $e")
        }

        hideProgressBar(event_detail_progressBar)
    }

    override fun onPause() {
        super.onPause()
        event_detail_map.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        event_detail_map.onLowMemory()
    }

    private suspend fun ownerFields(eventId: String) {
        val owner =
            getOwnerFromEvent(eventId)!!

        val title =
            "Organisateur - " + owner.clubAcronym + " " + owner.categoryName + " " + owner.subCategoryName
        event_detail_owner_club.text = title
        event_detail_owner_name.text = owner.coachFullname

        try {
            GlideApp.with(this).apply {
                load(UserAccount.pathToReference(owner.clubLogo))
                    .placeholder(R.drawable.ic_account)
                    .centerCrop()
                    .into(event_detail_owner_image)
            }
        } catch (e: Exception) {
            toast("Image could not load: $e")
        }
    }

    private fun showMap(position: String) {
        val uri =
            java.lang.String.format(
                Locale.FRANCE,
                "geo:0,0?q=$position"
            )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        requireContext().startActivity(intent)
    }

    private fun showProgressBar(progressBar: ProgressBar) {
        progressBar.apply {
            visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar(progressBar: ProgressBar) {
        progressBar.apply {
            visibility = View.GONE
        }
    }
}