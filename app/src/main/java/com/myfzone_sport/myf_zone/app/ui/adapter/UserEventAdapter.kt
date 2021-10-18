package com.myfzone_sport.myf_zone.app.ui.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.HomeUserEventCardviewBinding
import com.myfzone_sport.myf_zone.domain.event.Event

/**
 * Created by Amadou on 04/09/2021, 13:36
 */

class UserEventAdapter : ListAdapter<Event, UserEventAdapter.MyViewHolder>(
    TaskDiffCallBack()
) {

    private var eventList = emptyList<Event>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = eventList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    fun setData(list: List<Event>) {
        this.eventList = list
        notifyDataSetChanged()
    }

    class MyViewHolder(private val binding: HomeUserEventCardviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        //        @SuppressLint("RestrictedApi")
        fun bind(event: Event) {
            with(binding) {
                binding.event = event
            }

            binding.cardView.setOnClickListener {
                val bundle = bundleOf("eventId" to event.id, "coachRole" to "owner")
                when (it.findNavController().currentDestination?.label) {
//                    "Category List" -> navigate(R.id.categoryListFragmentToEventDetailsFragment, bundle, it)
                    "Category List" -> {
                        navigate(
                            R.id.categoryListFragmentToEventDetailsParticipantFragment,
                            bundle,
                            it
                        )
                        Log.i("user adapter","user adapter: Category List")
                    }
                    "Profil" -> {
                        navigate(R.id.profileFragmentToEventDetailsFragment, bundle, it)
                        Log.i("user adapter","user adapter: Profil")
                    }
//                    "Home" -> navigate(R.id.homeFragmentToEventDetailsFragment, bundle, it)
                    "Home" -> {
                        navigate(R.id.homeFragmentToEventDetailsOwner, bundle, it)
                        Log.i("user adapter","user adapter: Home")
                    }
                    "Map2" -> navigate(R.id.mapFragmentToEventDetailsFragment, bundle, it)
                    else -> {
                        Log.i("user adapter","user adapter: else")
                    }
                }
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
                    HomeUserEventCardviewBinding.inflate(layoutInflater, parent, false)
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