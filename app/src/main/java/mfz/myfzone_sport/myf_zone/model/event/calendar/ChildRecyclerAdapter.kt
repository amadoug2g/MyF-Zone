package mfz.myfzone_sport.myf_zone.model.event.calendar

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.card_event_profile.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.glide.GlideApp
import mfz.myfzone_sport.myf_zone.model.event.EventCalendar
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.isEventComplete
import mfz.myfzone_sport.myf_zone.util.user.UserAccount

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
                    load(UserAccount.pathToReference(eventCalendar.owner.clubLogo))
                        .placeholder(R.drawable.ic_account)
                        .centerCrop()
                        .into(itemView.cardView_clubImage_profile)
                }
            } catch (e: Exception) {
                Log.e("ParticipantAdapter", "Image could not load: $e")
            }

            CoroutineScope(Main).launch {
                try {
                    val status = isEventComplete(eventCalendar.id)
                    var dotBg: Int = R.drawable.notification_dot_red
                    when (status) {
                        false -> dotBg = R.drawable.notification_dot_blue
                        true -> dotBg = R.drawable.notification_dot_red
                    }
                    itemView.notificationDotOwner.setImageResource(dotBg)
                } catch (e: Exception) {
                    Log.e("ParticipantAdapter", "Image could not load: $e")
                }
            }


            val participate =
                itemView.cardView_clubName_profile.context.getString(R.string.participate_txt)
            val participantTitle =
                "$participate - ${eventCalendar.owner.clubAcronym} ${eventCalendar.owner.categoryName} ${eventCalendar.owner.subCategoryName}"

            itemView.cardView_clubName_profile.text = participantTitle
            itemView.cardView_clubDesc_profile.text = eventCalendar.owner.coachFullname
        }
    }
}
