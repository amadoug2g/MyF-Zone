package com.myfzone_sport.myf_zone.app.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isAffiliated
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isConnected
import com.myfzone_sport.myf_zone.databinding.ParticipantsPreviewLayoutBinding
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase

/**
 * Created by Amadou on 01/11/2021, 21:33
 */

class ParticipantsPreviewAdapter(private val getImageReferenceUseCase: GetImageReferenceUseCase, private val eventId: String) : ListAdapter<EventParticipant, ParticipantsPreviewAdapter.MyViewHolder>(
    TaskDiffCallBack()
) {

    private var participantList = mutableListOf<EventParticipant>()
    private var limit = 3

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = participantList[position]
        holder.bind(getImageReferenceUseCase.invoke(currentItem.clubLogo), eventId)
    }

    override fun getItemCount(): Int {
        return if (participantList.size > limit) {
            limit
        } else {
            participantList.size
        }
    }

    fun setData(list: MutableList<EventParticipant>) {
        if (list.isNotEmpty()) this.participantList = list
        notifyDataSetChanged()
    }

    class MyViewHolder(private val binding: ParticipantsPreviewLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(path: StorageReference, eventId: String) {

            val bundle = bundleOf("eventId" to eventId)

            binding.participantImage.setOnClickListener {
                when (it.findNavController().currentDestination?.label) {
                    "Event Details Participant" -> {
                        navigate(R.id.eventDetailsToEventParticipants, bundle, it)
                    }
                    "Event Details Guest" -> {
                        navigate(R.id.eventDetailsGuestToEventParticipants, bundle, it)
                    }
                }
            }

            GlideApp.with(binding.participantImage).apply {
                load(path)
                    .centerCrop()
                    .into(binding.participantImage)
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
                    ParticipantsPreviewLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    class TaskDiffCallBack : DiffUtil.ItemCallback<EventParticipant>() {
        override fun areItemsTheSame(
            oldItem: EventParticipant,
            newItem: EventParticipant
        ): Boolean {
            return oldItem.coachId == newItem.coachId;
        }

        override fun areContentsTheSame(
            oldItem: EventParticipant,
            newItem: EventParticipant
        ): Boolean {
            return oldItem == newItem
        }
    }
}