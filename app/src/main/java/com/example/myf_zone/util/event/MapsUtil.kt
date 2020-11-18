package com.example.myf_zone.util.event

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.myf_zone.R
import com.example.myf_zone.model.coach.ClubAffiliation
import com.example.myf_zone.model.event.Event
import com.example.myf_zone.util.club.ClubUtil.getClubById
import com.example.myf_zone.util.event.EventUtil.getEventById
import com.example.myf_zone.util.event.EventUtil.getEventsByDate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import java.net.URL
import java.util.*


object MapsUtil {

    private val TAG = MapsUtil::class.java.simpleName

    fun initializeMap(
        map: GoogleMap,
        onMarkerClickListener: GoogleMap.OnMarkerClickListener,
        onMapClickListener: GoogleMap.OnMapClickListener,
        markerList: MutableList<Marker>,
        cardView: MaterialCardView
    ) {
        setMapUIControls(map)
        mapStartPosition(
            map,
            markerList,
            cardView
        )
        setMapListeners(
            map, markerList,
            onMarkerClickListener,
            onMapClickListener, cardView
        )
    }

    suspend fun placeEventsOnMap(map: GoogleMap, context: Context): MutableList<Event>? {
        return try {
            val eventList = getEventsByDate()

//            if (globalEventList == eventList) {
//
//            }

            if (!eventList.isNullOrEmpty()) {
                for (event in eventList) {
                    val markerOptions = setEventMarkerOptions(event, context)
                    map.addMarker(markerOptions).tag = event.id
                    Log.d(TAG, "Marker is: ${event.id}")
                }

//                Log.d(TAG, "Event List: $eventList")
                eventList
            } else {
                Log.d(TAG, "List is null or empty: $eventList")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getEventsByDate(): $e")
            null
        }
    }

    fun placeUserClub(clubAffiliation: ClubAffiliation, map: GoogleMap, context: Context) {
        try {
            getClubById(clubAffiliation.clubId) {

//                CoroutineScope(Main).launch {
//                    val bitmap = getImageClub(it.logo)!!
                val markerOptions = MarkerOptions().apply {
                    position(it.getPosition())
                    title(it.acronym)
//                        icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    snippet(it.name)
//                    icon(BitmapDescriptorFactory
//                        .fromBitmap(BitmapFactory
//                            .decodeResource(context.resources, setMarkerType(event))))
                }
                map.addMarker(markerOptions).tag = null//.setIcon(getImageClub(it.logo)!!)
//                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in placeUserClub: $e")
        }
    }

    fun getEventImage(imageView: ImageView, event: Event) {
        val clubLogo = event.owner.clubLogo
        val url = URL(clubLogo)
        val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
        imageView.setImageBitmap(bmp)
    }

    fun getCardDetail(
        marker: Marker,
        cardView: MaterialCardView,
        title: TextView,
        description: TextView,
        image: ImageView,
        tag: TextView
    ) {
        Log.d(TAG, "Marker Id is: ${marker.tag}")
        when (marker.tag) {
            null -> {
                cardView.visibility = View.GONE
                tag.text = null
            }
            else -> {
                getEventMarkerDetails(
                    marker,
                    cardView,
                    title,
                    description,
                    image,
                    tag
                )
                tag.text = marker.tag as String
            }
        }
    }

    private fun getEventMarkerDetails(
        marker: Marker,
        cardView: MaterialCardView,
        title: TextView,
        description: TextView,
        image: ImageView,
        tag: TextView
    ) {
        cardView.apply {
            visibility = View.VISIBLE
//            startAnimation(AnimationUtils.loadAnimation(context, R.anim.from_bottom))
        }

        title.text = (R.string.loading).toString()
        description.text = (R.string.loading).toString()
//        image.setImageResource(R.mipmap.ic_football_ball_icon_001)

        try {
            getEventById(marker.tag as String) { event ->
                title.text = event.title
                description.text = event.description
                image.setImageResource(
                    setMarkerType(
                        event
                    )
                )
                Log.d(TAG, "Marker Title is: ${event.title}")
                Log.d(TAG, "Marker Description is: ${event.description}")
//                Log.d(TAG, "Marker Type is: ${setMarkerType(event)}")
            }
            tag.text = marker.tag as String
        } catch (e: Exception) {
            Log.e(TAG, "Error in getEventFromId: $e")
        }
    }

    private fun zoomOnMarker(map: GoogleMap, position: LatLng, zoom: Float = 14f): Boolean {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom))
        return true
    }

    fun setMarkerType(event: Event): Int {
        var result: Int = R.mipmap.ic_football_ball_icon_001
        when (event.type) {
            "friendly" -> result = R.mipmap.ic_football_ball_icon_002
            "tournament" -> result = R.mipmap.ic_football__trophy_icon_002
            "plateau" -> result = R.mipmap.ic_football_field_icon_002
        }
        return result
    }

    private fun setEventMarkerOptions(event: Event, context: Context): MarkerOptions {
        return MarkerOptions().apply {
            position(event.getPosition())
            title(event.getAcronym())
            snippet(event.title)
            icon(
                BitmapDescriptorFactory
                    .fromBitmap(
                        BitmapFactory
                            .decodeResource(context.resources, setMarkerType(event))
                    )
            )
        }
    }

    private fun setMapUIControls(map: GoogleMap) {
        map.uiSettings.apply {
//            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
            isMyLocationButtonEnabled = true
        }
    }

    fun setMapListeners(
        map: GoogleMap,
        markerList: MutableList<Marker>,
        onMarkerClickListener: GoogleMap.OnMarkerClickListener,
        onMapClickListener: GoogleMap.OnMapClickListener,
        cardView: MaterialCardView
    ) {
//        map.setOnMarkerClickListener {
//            getMarkerDetails(
//                marker,
//                cardView_detail,
//                cardView_clubName,
//                cardView_clubDesc,
//                cardView_clubImage,
//                requireContext()
//            )
//            zoomOnMarker(map, it.position)
//        }
        map.apply {
            setOnMarkerClickListener(onMarkerClickListener)
            setOnMapClickListener(onMapClickListener)
            setMaxZoomPreference(16f)
            setMinZoomPreference(5f)
            setOnCameraMoveListener {
                if (map.cameraPosition.zoom > 9) {
                    showMarkers(
                        markerList
                    )
                } else {
                    hideMarkers(
                        markerList
                    )
                    cardView.visibility = View.GONE
                }
            }
        }
    }

    private fun mapStartPosition(
        map: GoogleMap,
        markerList: MutableList<Marker>,
        cardView: MaterialCardView
    ) {
        if (map.cameraPosition.zoom > 9) {
            showMarkers(markerList)
        } else {
            hideMarkers(markerList)
            cardView.visibility = View.GONE
        }

        val position = LatLng(48.8550, 2.3452)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 9.5f))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 10.5f))
    }

    private fun hideMarkers(list: MutableList<Marker>) {
        for (i in list) {
            i.isVisible = false
        }
    }

    private fun showMarkers(list: MutableList<Marker>) {
        for (i in list) {
            i.isVisible = true
        }
    }

    private fun stampToDate(time: Timestamp): Date {
        return time.toDate()
    }
}