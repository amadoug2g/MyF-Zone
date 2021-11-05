package com.myfzone_sport.myf_zone.app.ui.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.HomeCloseToClubCardviewBinding
import com.myfzone_sport.myf_zone.domain.event.Event

/**
 * Created by Amadou on 03/09/2021, 21:51
 */

class CloseToClubEventAdapter(val context: Context, val savedInstanceState: Bundle?) : ListAdapter<Event, CloseToClubEventAdapter.MyViewHolder>(
    TaskDiffCallBack()
) {

    private var eventList = mutableListOf<Event>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = eventList[position]
        holder.bind(currentItem, context, savedInstanceState)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    fun setData(list: MutableList<Event>) {
        if (list.isNotEmpty()) this.eventList = list
        notifyDataSetChanged()
    }

    class MyViewHolder(private val binding: HomeCloseToClubCardviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event, context: Context, savedInstanceState: Bundle?) {
            with(binding) {
                binding.event = event
            }

            mapPreview(event, context, savedInstanceState)

            binding.cardView.setOnClickListener {
                val bundle = bundleOf("eventId" to event.id)
                navigate(R.id.homeFragmentToEventDetailsFragment, bundle, it)
            }
        }

        private fun mapPreview(event: Event, context: Context, savedInstanceState: Bundle?) {
            binding.eventDetailMap.eventDetailMap.onCreate(savedInstanceState)
            binding.eventDetailMap.eventDetailMap.onResume()

            MapsInitializer.initialize(context)

            binding.eventDetailMap.eventDetailMap.getMapAsync { map ->
                val markerOptions = MarkerOptions().apply {
                    position(event.getPosition())
                    snippet(event.address)
                    icon(BitmapDescriptorFactory.fromBitmap(generateSmallIcon(context)))
                }

                map.apply {
                    uiSettings.apply {
                        setAllGesturesEnabled(false)
                        isZoomControlsEnabled = false
                        isRotateGesturesEnabled = false
                        isScrollGesturesEnabled = false
                        isScrollGesturesEnabledDuringRotateOrZoom = false
                        isZoomControlsEnabled = false
                        isTiltGesturesEnabled = false
                    }

                    setPadding(0, 0, 0, 40)
                    addMarker(markerOptions)
                    moveCamera(CameraUpdateFactory.newLatLngZoom(event.getPosition(), 13f))
                    setOnMarkerClickListener { true }
                }
            }
        }

//        fun generateHomeMarker(context: Context): MarkerOptions {
//            return MarkerOptions()
//                .icon(BitmapDescriptorFactory.fromBitmap(generateSmallIcon(context)))
//        }

        private fun generateSmallIcon(context: Context): Bitmap {
            val height = 56
            val width = 35
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.marker)
            return Bitmap.createScaledBitmap(bitmap, width, height, false)
        }

        private fun navigate(destination: Int, extra: Bundle? = null, view: View) {
            Navigation
                .findNavController(view)
                .navigate(destination, extra)
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    HomeCloseToClubCardviewBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    class TaskDiffCallBack : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
//            Log.d("TAG", Thread.currentThread().name)
            return oldItem.id == newItem.id;
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
//            Log.d("TAG", Thread.currentThread().name)
            return oldItem == newItem
        }
    }
}