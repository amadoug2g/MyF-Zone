package com.myfzone_sport.myf_zone.domain.maps

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.myfzone_sport.myf_zone.domain.event.Event

class MyClusterItem(
    private val title: String = "",
    private val snippet: String = "",
    val tag: String = "",
    val event: Event
) : ClusterItem {

    constructor() : this("", "", "", Event())

    override fun getSnippet(): String {
        return snippet
    }

    override fun getTitle(): String {
        return title
    }

    override fun getPosition(): LatLng {
        return event.getPosition()
    }
}
