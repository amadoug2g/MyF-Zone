package com.myfzone_sport.myf_zone.model.maps

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterItem
import com.myfzone_sport.myf_zone.model.event.Event

class MapClusterItem(
    private val event: Event,
    private val marker: Marker
) : ClusterItem {

    override fun getSnippet(): String {
        return event.description
    }

    override fun getTitle(): String {
        return event.title
    }

    override fun getPosition(): LatLng {
        return LatLng(event.lat, event.lng)
    }

    val tag: String
        get() {
            return event.id
        }
}