package mfz.myfzone_sport.myf_zone.fragments.maps.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.CardEventProfileBinding
import mfz.myfzone_sport.myf_zone.glide.GlideApp
import mfz.myfzone_sport.myf_zone.model.event.Event

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
            val bundle = bundleOf("eventId" to item.id)
            navigate(R.id.profileToEventDetails, bundle, holder.itemView)
        }
    }

    class ProfileEventViewHolder(val binding: CardEventProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            with(binding) {
                binding.event = event

                //TODO: move getImageReference to ProfileViewModel
                GlideApp.with(itemView).apply {
                    load(ProfileService.getImageReference(event.owner.clubLogo))
                        .centerCrop()
                        .into(binding.cardViewClubImageProfile)
                }

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