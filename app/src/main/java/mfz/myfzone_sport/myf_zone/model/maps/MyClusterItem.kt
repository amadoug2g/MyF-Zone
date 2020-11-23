package mfz.myfzone_sport.myf_zone.model.maps

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class MyClusterItem(
    private val lat: Double,
    private val lng: Double,
    private val title: String = "",
    private val snippet: String = ""
) : ClusterItem {

    constructor() : this(0.0, 0.0, "", "")

    override fun getSnippet(): String? {
        return snippet
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getPosition(): LatLng {
        return LatLng(lat, lng)
    }
}
