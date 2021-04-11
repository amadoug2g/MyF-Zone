package com.myfzone_sport.myf_zone.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.CardEventProfileBinding
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking

/**
 * Created by Amadou on 02/12/2020, 18:01
 *
 * Profile Event Adapter Class
 *
 */

class ProfileEventAdapter :
    ListAdapter<Event, ProfileEventAdapter.ProfileEventViewHolder>(ProfileEventDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileEventViewHolder {
        return ProfileEventViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ProfileEventViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        holder.binding.cardViewDetailProfile.setOnClickListener {
            TRACKING.logEvent(Tracking.ACCOUNT_OPEN_EVENT, null)
            val bundle = bundleOf("eventId" to item.id)
            navigate(R.id.profileToEventDetailsOwner, bundle, holder.itemView)
//            navigate(R.id.profileToEventDetails, bundle, holder.itemView)
        }
    }

    class ProfileEventViewHolder(val binding: CardEventProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            with(binding) {
                binding.event = event

                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ProfileEventViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CardEventProfileBinding.inflate(layoutInflater, parent, false)
                return ProfileEventViewHolder(binding)
            }
        }
    }

    class ProfileEventDiffCallBack : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }

    }

    private fun navigate(destination: Int, extra: Bundle? = null, view: View) {
        Navigation
            .findNavController(view)
            .navigate(destination, extra)
    }
}