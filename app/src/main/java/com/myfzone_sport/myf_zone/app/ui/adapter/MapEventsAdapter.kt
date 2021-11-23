package com.myfzone_sport.myf_zone.app.ui.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.CardMapItemBinding
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.detailevent.GetOwnerFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import java.lang.Exception

/**
 * Created by Amadou on 06/09/2021, 03:03
 */

class MapEventsAdapter(private val getImageReferenceUseCase: GetImageReferenceUseCase) : ListAdapter<Event, MapEventsAdapter.MyViewHolder>(
    TaskDiffCallBack()
) {

    private var eventList = emptyList<Event>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = eventList[position]
        holder.bind(currentItem, getImageReferenceUseCase)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    fun setData(list: List<Event>) {
        this.eventList = list
        notifyDataSetChanged()
    }

    class MyViewHolder(private val binding: CardMapItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event, getImageReferenceUseCase: GetImageReferenceUseCase) {
            with(binding) {
                this.event = event
                this.owner = event.owner
            }

            binding.eventDetailBtn.setOnClickListener {
                val bundle = bundleOf("eventId" to event.id)
                navigate(R.id.mapFragmentToEventDetailsFragment, bundle, it)
            }

            try {
                GlideApp.with(binding.cardViewDetail).apply {
                    load(getImageReferenceUseCase.invoke(event.owner.clubLogo))
                        .centerCrop()
                        .into(binding.cardViewClubImage)
                }
            } catch (e: Exception) {
                Log.i("tagging", "error: $e")
            }
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
                    CardMapItemBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    class TaskDiffCallBack : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            Log.d("TAG", Thread.currentThread().name)
            return oldItem.id == newItem.id;
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            Log.d("TAG", Thread.currentThread().name)
            return oldItem == newItem
        }
    }
}