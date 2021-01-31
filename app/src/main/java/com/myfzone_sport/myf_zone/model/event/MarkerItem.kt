package com.myfzone_sport.myf_zone.model.event

import com.google.android.gms.maps.model.Marker

data class MarkerItem(
    var marker: Marker,
    var day: Long
) {
    fun inRange(first: Long, second: Long): Boolean {
        return (day - first > 0 && second - day < 0)
    }

    override fun toString(): String {
        return "${marker.title} - ${marker.snippet}"
    }
}
