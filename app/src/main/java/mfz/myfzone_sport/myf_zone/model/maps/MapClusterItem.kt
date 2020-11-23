package mfz.myfzone_sport.myf_zone.model.maps

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import mfz.myfzone_sport.myf_zone.model.event.Event

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