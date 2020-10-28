package com.example.myf_zone.util

import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.myf_zone.R
import com.example.myf_zone.model.event.Event
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.card.MaterialCardView
import java.net.URL


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
        if (map.cameraPosition.zoom > 10.5) {
            showMarkers(markerList)
        } else {
            hideMarkers(markerList)
            cardView.visibility = View.GONE
        }
        setMapListeners(map, onMarkerClickListener, onMapClickListener, markerList, cardView)

        val position = LatLng(48.8550, 2.3452)
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10f))
    }

    fun placeEventOnMap(
        map: GoogleMap,
        event: Event,
        context: Context,
        drawable: Any = ""
    ): Marker {
        val marker: Marker = map.addMarker(setEventMarkerOptions(event, context))
        if (drawable is Int)
            marker.tag = drawable
        if (drawable is String)
            marker.tag = setMarkerType(event)
        return marker
    }

    //Working with real events
    fun placeEventOnMapFinal(map: GoogleMap, event: Event, context: Context): Marker {
        val marker: Marker = map.addMarker(setEventMarkerOptions(event, context))
        marker.tag = event.owner.clubLogo
        return marker
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
        cardView.visibility = View.VISIBLE
        title.text = marker.title
        description.text = marker.snippet
        image.setImageBitmap(BitmapFactory.decodeResource(context.resources, marker.tag as Int))
//        getEventImage(image, event)
    }

    fun addItem(list: MutableList<Marker>, vararg item: Marker) {
        for (i in item) {
            list.add(i)
        }
    }

    private fun setMarkerType(event: Event): Int {
        var result: Int = R.mipmap.ic_football_ball_icon_001
        when (event.type) {
            "Friendly" -> result = R.mipmap.ic_football_ball_icon_002
            "Tournament" -> result = R.mipmap.ic_football__trophy_icon_002
            "Plateau" -> result = R.mipmap.ic_football_field_icon_002
        }
        return result
    }

    private fun setEventMarkerOptions(event: Event, context: Context): MarkerOptions {
        return MarkerOptions()
            .position(LatLng(event.lat, event.lng))
            .title(event.owner.clubAcronym)
            .snippet(event.title)
            .icon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(context.resources, setMarkerType(event))
                )
            )
    }

    private fun setMapUIControls(map: GoogleMap) {
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isMapToolbarEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
    }

    private fun setMapListeners(
        map: GoogleMap,
        onMarkerClickListener: GoogleMap.OnMarkerClickListener,
        onMapClickListener: GoogleMap.OnMapClickListener,
        markerList: MutableList<Marker>,
        cardView: MaterialCardView
    ) {
        map.setOnMarkerClickListener(onMarkerClickListener)
        map.setOnMapClickListener(onMapClickListener)
        map.setMaxZoomPreference(16f)
        map.setMinZoomPreference(5f)
        map.setOnCameraMoveListener {
            if (map.cameraPosition.zoom > 10.5) {
                showMarkers(markerList)
            } else {
                hideMarkers(markerList)
                cardView.visibility = View.GONE
            }
        }
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

}