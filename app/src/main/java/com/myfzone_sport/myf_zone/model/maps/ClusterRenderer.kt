package com.myfzone_sport.myf_zone.model.maps

import android.content.Context
import android.graphics.BitmapFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.myfzone_sport.myf_zone.model.event.Event

/**
 * Created by Amadou on 28/01/2021, 19:36
 *
 * TODO: File Description
 *
 */

class ClusterRenderer(
    context: Context,
    map: GoogleMap?,
    clusterManager: ClusterManager<MyClusterItem>?
) : DefaultClusterRenderer<MyClusterItem>(context, map, clusterManager) {

    private val mContext: Context = context

    override fun onBeforeClusterItemRendered(item: MyClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        setEventMarkerOptions(item.event)
    }

    private fun setEventMarkerOptions(event: Event): MarkerOptions {
        return MarkerOptions().apply {
            icon(
                BitmapDescriptorFactory
                    .fromBitmap(
                        BitmapFactory
                            .decodeResource(mContext.resources, event.eventTypeImage)
                    )
            )
        }
    }
}