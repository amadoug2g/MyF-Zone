package com.myfzone_sport.myf_zone.model.maps

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * Created by Amadou on 28/01/2021, 19:36
 *
 * : handles Marker Cluster Rendering
 *
 */

class ClusterRenderer(
    context: Context,
    map: GoogleMap?,
    clusterManager: ClusterManager<MyClusterItem>?
) : DefaultClusterRenderer<MyClusterItem>(context, map, clusterManager) {

    private val mContext: Context = context
    private val TAG = this::class.java.simpleName

    override fun onBeforeClusterItemRendered(item: MyClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        val eventIcon: BitmapDescriptor by lazy {
            BitmapHelper.eventToBitmap(mContext, item.event)
        }

        markerOptions.apply {
            icon(eventIcon)
        }
    }

}