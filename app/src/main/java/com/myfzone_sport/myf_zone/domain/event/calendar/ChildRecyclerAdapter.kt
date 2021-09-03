package com.myfzone_sport.myf_zone.domain.event.calendar

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
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth.isAffiliated
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth.isConnected
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.domain.event.EventCalendar
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.android.synthetic.main.card_event_calendar.view.*
import kotlinx.android.synthetic.main.card_event_profile.view.notificationDotOwner

class ChildRecyclerAdapter(private val items: MutableList<EventCalendar>) :
    RecyclerView.Adapter<ChildRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.card_event_calendar, parent, false)

        return ViewHolder(
            view
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = items[position]
        holder.bind(event)

        holder.cardview.setOnClickListener {
//            val owner = getOwner(event.id)
            TRACKING.logEvent(Tracking.AGENDA_OPEN_EVENT, null)
            val bundle = bundleOf("eventId" to event.id)
//            navigate(R.id.calendarToEventDetail, bundle, holder.itemView)


//            val ownerId = MapsViewModel.getOwnerFromEvent(event.id)

            try {
                if (isConnected) {
                    if (isAffiliated) {
                        if (ManagerAuth.isCoachOwner(event.id)) {
                            navigate(R.id.calendarToEventDetailsOwner, bundle, holder.itemView)
                        } else {
                            navigate(
                                R.id.calendarToEventDetailsParticipant,
                                bundle,
                                holder.itemView
                            )
                        }
                    } else {
                        navigate(R.id.calendarToEventDetailsGuest, bundle, holder.itemView)
                    }
                } else {
                    navigate(R.id.calendarToEventDetailsGuest, bundle, holder.itemView)
                }
            } catch (e: Exception) {
                Log.e("Error", "ERROR: ${e.localizedMessage}")
            }

        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var titleView: TextView = view.cardView_eventType
        var descriptionView: TextView = view.cardView_eventTitle
        var clubImage: ImageView = view.cardView_eventImage
        var cardview: MaterialCardView = view.cardView_detail_agenda

        fun bind(eventCalendar: EventCalendar) {

            try {
                GlideApp.with(itemView.context).apply {
                    load(eventCalendar.eventTypeImage)
                        .placeholder(R.drawable.ic_account)
                        .centerCrop()
                        .into(itemView.cardView_eventImage)
                }
            } catch (e: Exception) {
                Log.e("ViewHolder", "Image could not load: $e")
            }

//            try {
//                GlideApp.with(itemView).apply {
//                    load(getImageReference(eventCalendar.owner.clubLogo))
//                        .placeholder(R.drawable.ic_account)
//                        .centerCrop()
//                        .into(itemView.cardView_clubImage)
//                }
//            } catch (e: Exception) {
//                Log.e("CalendarClubImage", "Image could not load: $e")
//            }

            try {
                if (ManagerAuth.isCoachOwner(eventCalendar.id)) {
                    itemView.notificationDotOwner.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("ParticipantAdapter", "Image could not load: $e")
            }

/*

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
*/

            itemView.cardView_eventType.text = eventCalendar.eventTypeString
            itemView.cardView_eventTitle.text = eventCalendar.title
        }
    }

    private fun navigate(destination: Int, extra: Bundle? = null, view: View) {
        Navigation
            .findNavController(view)
            .navigate(destination, extra)
    }
}
