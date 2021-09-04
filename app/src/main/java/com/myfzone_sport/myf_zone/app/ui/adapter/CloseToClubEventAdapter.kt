package com.myfzone_sport.myf_zone.app.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myfzone_sport.myf_zone.databinding.HomeCloseToClubCardviewBinding
import com.myfzone_sport.myf_zone.domain.event.Event

/**
 * Created by Amadou on 03/09/2021, 21:51
 */

class CloseToClubEventAdapter : ListAdapter<Event, CloseToClubEventAdapter.MyViewHolder>(
    TaskDiffCallBack()
) {

    private var eventList = mutableListOf<Event>()

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

    fun setData(list: MutableList<Event>) {
        this.eventList = list
        notifyDataSetChanged()
    }

    class MyViewHolder(private val binding: HomeCloseToClubCardviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            with(binding) {
                binding.event = event
            }

            binding.cardView.setOnClickListener {
//                val action = ListFragmentDirections.listFragmentToDetailFragment(event.id)
//                navController.navigate(action)

                Log.i("", "Clicked ${event.title}")
            }
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
            Log.d("TAG", Thread.currentThread().name)
            return oldItem.id == newItem.id;
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            Log.d("TAG", Thread.currentThread().name)
            return oldItem == newItem
        }
    }
}