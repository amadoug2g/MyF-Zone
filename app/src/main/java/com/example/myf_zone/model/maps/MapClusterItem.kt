package com.example.myf_zone.model.maps

import com.example.myf_zone.model.event.Event
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class MapClusterItem(
    private val event: Event
) : ClusterItem {
    constructor() : this(Event())

    override fun getSnippet(): String? {
        return event.description
    }

    override fun getTitle(): String? {
        return event.title
    }

    override fun getPosition(): LatLng {
        return LatLng(event.lat, event.lng)
    }
}