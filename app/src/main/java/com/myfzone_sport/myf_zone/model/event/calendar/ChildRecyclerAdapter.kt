package com.myfzone_sport.myf_zone.model.event.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.model.event.EventCalendar
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.android.synthetic.main.card_event_profile.view.*

class ChildRecyclerAdapter(private val items: MutableList<EventCalendar>) :
    RecyclerView.Adapter<ChildRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.card_event_profile, parent, false)
        return ViewHolder(
            view
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = items[position]
        holder.bind(event)

        holder.cardview.setOnClickListener {
            TRACKING.logEvent(Tracking.AGENDA_OPEN_EVENT, null)
            val bundle = bundleOf("eventId" to event.id)
            navigate(R.id.calendarToEventDetail, bundle, holder.itemView)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var titleView: TextView = view.cardView_clubName_profile
        var descriptionView: TextView = view.cardView_clubDesc_profile
        var clubImage: ImageView = view.cardView_clubImage_profile
        var cardview: MaterialCardView = view.cardView_detail_profile

        fun bind(eventCalendar: EventCalendar) {

            try {
                GlideApp.with(itemView.context).apply {
                    load(eventCalendar.eventTypeImage)
                        .placeholder(R.drawable.ic_account)
                        .centerCrop()
                        .into(itemView.cardView_clubImage_profile)
                }
            } catch (e: Exception) {
                Log.e("ViewHolder", "Image could not load: $e")
            }

//            CoroutineScope(Main).launch {
//                try {
//                    val status = isEventComplete(eventCalendar.id)
//                    var dotBg: Int = R.drawable.notification_dot_red
//                    when (status) {
//                        false -> dotBg = R.drawable.notification_dot_blue
//                        true -> dotBg = R.drawable.notification_dot_red
//                    }
//                    itemView.notificationDotOwner.setImageResource(dotBg)
//                } catch (e: Exception) {
//                    Log.e("ParticipantAdapter", "Image could not load: $e")
//                }
//            }

//            CoroutineScope(IO).launch {
//                try {
//                    val status = isEventComplete(eventCalendar.id)
//                    var dotBg: Int = R.drawable.notification_dot_red
//                    if (status != null) {
//                        dotBg = when (status) {
//                            false -> R.drawable.notification_dot_blue
//                            true -> R.drawable.notification_dot_red
//                        }
//                        itemView.notificationDotOwner.setImageResource(dotBg)
//                    }
//                } catch (e: Exception) {
//                    Log.e("EventStatus", "Event status could not be retrieved: $e")
//                }
//            }

            itemView.cardView_clubName_profile.text = eventCalendar.eventTypeString
            itemView.cardView_clubDesc_profile.text = eventCalendar.title
        }
    }

    private fun navigate(destination: Int, extra: Bundle? = null, view: View) {
        Navigation
            .findNavController(view)
            .navigate(destination, extra)
    }
}
