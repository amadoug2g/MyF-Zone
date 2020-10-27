package com.example.myf_zone.util

import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.myf_zone.R
import com.example.myf_zone.model.event.Event
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.card.MaterialCardView

object MapsUtil {

    private val TAG = MapsUtil::class.java.simpleName

    private lateinit var map: GoogleMap
    private lateinit var markerOptions: GoogleMapOptions

    fun placeMarkerOnMap(map: GoogleMap, markerOptions: MarkerOptions): Marker {
        val marker: Marker = map.addMarker(markerOptions)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.position, 11f))
        return marker
    }

    fun initializeMap(
        map: GoogleMap,
        onMarkerClickListener: GoogleMap.OnMarkerClickListener,
        onMapClickListener: GoogleMap.OnMapClickListener,
        markerList: MutableList<Marker>,
        cardView: MaterialCardView
    ) {
        val position = LatLng(48.8550, 2.3452)

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isMapToolbarEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        hideMarkers(markerList)

        map.setOnMarkerClickListener(onMarkerClickListener)
        map.setOnMapClickListener(onMapClickListener)
        map.setMaxZoomPreference(16f)
        map.setMinZoomPreference(5f)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10f))
        map.setOnCameraMoveListener {
            if (map.cameraPosition.zoom > 11) {
                showMarkers(markerList)
            } else {
                hideMarkers(markerList)
                cardView.visibility = View.GONE
            }
        }
    }

    fun placeMarkerOnMapFromOptions(map: GoogleMap, marker: Marker, markerOptions: MarkerOptions) {
        map.addMarker(
            markerOptions
                .position(markerOptions.position)
                .title(markerOptions.title)
        )

//        markerOptions.icon
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.position, 11f))
    }

    fun setMarkerFromEvent() {

    }

    fun placeEventOnMap(event: Event, context: Context) {
//        val markerOptions = MarkerOptions().position(location)
//        map.addMarker(markerOptions)

        val markerOptions = MarkerOptions()
            .position(LatLng(event.lat, event.lng))
            .title(event.title)
            .icon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(context.resources, R.mipmap.ic_fc93_logo)
                )
            )

//        markerOptions.icon
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.position, 11f))
    }

    fun getMarkerDetails(
        marker: Marker,
        cardView: MaterialCardView,
        title: TextView,
        image: ImageView,
        context: Context,
        drawable: Int
    ) {
        cardView.visibility = View.VISIBLE
        title.text = marker.title
//        val bitImage : Bitmap?

//        image.setImageBitmap(markerOptions.icon.)
        image.setImageBitmap(BitmapFactory.decodeResource(context.resources, drawable))
    }

    fun clearMapOnZoom() {
        this.map.clear()
    }

    fun addItem(list: MutableList<Marker>, vararg item: Marker) {
        for (i in item) {
            list.add(i)
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