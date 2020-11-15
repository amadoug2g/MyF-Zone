package com.example.myf_zone.util.event

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.myf_zone.R
import com.example.myf_zone.model.event.Event
import com.example.myf_zone.model.event.EventParticipant
import com.example.myf_zone.util.event.EventUtil.getEvents
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
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

    private fun placeEventOnMap(
        map: GoogleMap,
        event: Event,
        context: Context,
        drawable: Any = ""
    ): Marker {
        Log.d(TAG, "EVENT: ${event.title}")
        val marker: Marker = map.addMarker(
            setEventMarkerOptions(
                event,
                context
            )
        )
        if (drawable is Int)
            marker.tag = drawable
        if (drawable is String)
            marker.tag = event.eventTypeImage
        return marker
    }

    fun placeFirestoreEvent(
        context: Context,
        map: GoogleMap,
        owner: EventParticipant,
        list: MutableList<EventParticipant>,
        markerList: MutableList<Marker>
    ): MutableList<Event> {
        val task = getEvents()
        val resultList = mutableListOf<Event>()
        var newEvent: Event
        task.addOnCompleteListener {
            if (task.isSuccessful) {

                val documentList = it.result.documents

                for (doc in documentList) {
                    Toast.makeText(context, doc["title"].toString(), Toast.LENGTH_SHORT).show()

//                    newEvent = placeEventFromFirestoreOnMap(doc, owner, list)
//                    markerList.add(
//                        placeEventOnMap(
//                            map,
//                            newEvent,
//                            context
//                        )
//                    )
                }
            }
        }
        return resultList
    }

    private fun placeEventFromFirestoreOnMap(
        doc: DocumentSnapshot,
        owner: EventParticipant,
        list: MutableList<EventParticipant>
    ): Event {

        val event = doc.toObject<Event>()!!
        event.owner = owner
        event.participants = list

        Log.d(TAG, "Event ID is ${doc.id}")

        return event

//        val nbTeam = (doc["nbTeam"] as Long).toInt()
//        val date: Date = stampToDate(doc["date"] as Timestamp)
//        val createdDate: Date = stampToDate(doc["createdDate"] as Timestamp)
//
//        return Event(
//            doc["title"] as String,
//            doc["description"] as String,
//            doc["type"] as String,
//            nbTeam,
//            date,
//            doc["address"] as String,
//            doc["lat"] as Double,
//            doc["lng"] as Double,
//            createdDate,
//            owner,
//            list
//        )
    }

    fun getEventImage(imageView: ImageView, event: Event) {
        val clubLogo = event.owner.clubLogo
        val url = URL(clubLogo)
        val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
        imageView.setImageBitmap(bmp)
    }

    fun getMarkerDetails(
        marker: Marker,
        cardView: MaterialCardView,
        title: TextView,
        description: TextView,
        image: ImageView,
        context: Context
    ) {
        cardView.apply {
            visibility = View.VISIBLE
//            startAnimation(AnimationUtils.loadAnimation(context, R.anim.from_bottom))
        }
        title.text = marker.title
        description.text = marker.snippet
        image.setImageBitmap(BitmapFactory.decodeResource(context.resources, marker.tag as Int))
    }

    fun getEventMarkerDetails(
        event: Event,
        cardView: MaterialCardView,
        title: TextView,
        description: TextView,
        image: ImageView
    ) {
        cardView.apply {
            visibility = View.VISIBLE
//            startAnimation(AnimationUtils.loadAnimation(context, R.anim.from_bottom))
        }
        title.text = event.title
        description.text = event.description
        image.setImageResource(
            setMarkerType(
                event
            )
        )
    }

    fun addItem(list: MutableList<Marker>, vararg item: Marker) {
        for (i in item) {
            list.add(i)
        }
    }

    private fun addItemEvent(vararg item: Event): MutableList<Event> {
        val list = mutableListOf<Event>()
        for (i in item) {
            list.add(i)
        }
        return list
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
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(
                        context.resources,
                        setMarkerType(
                            event
                        )
                    )
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

    private fun setMapListeners(
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