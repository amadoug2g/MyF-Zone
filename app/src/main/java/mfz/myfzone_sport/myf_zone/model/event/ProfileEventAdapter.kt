package mfz.myfzone_sport.myf_zone.model.event

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
import kotlinx.android.synthetic.main.card_event_profile.view.*
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.glide.GlideApp
import mfz.myfzone_sport.myf_zone.util.user.UserAccount

class ProfileEventAdapter(private val coachEventList: MutableList<Event>) :
    RecyclerView.Adapter<ProfileEventAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.card_event_profile, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return coachEventList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val owner = coachEventList[position].owner
        val event = coachEventList[position]
        val title =
            holder.itemView.context.getString(R.string.owner_txt) + " - " + owner.clubAcronym + " " + owner.categoryName + " " + owner.subCategoryName

        try {
            GlideApp.with(holder.itemView.context).apply {
                load(UserAccount.pathToReference(owner.clubLogo))
                    .placeholder(R.drawable.ic_account)
                    .centerCrop()
                    .into(holder.image)
            }
        } catch (e: Exception) {
            Log.e("ProfileEventAdapter", "Image could not load: $e")
        }

        holder.title.text = title
        holder.subtitle.text = event.eventTypeString

        holder.cardview.setOnClickListener {
            val bundle = bundleOf("eventId" to event.id)
            navigate(R.id.profileToEventDetails, bundle, holder.itemView)
        }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.cardView_clubImage_profile
        var title: TextView = view.cardView_clubName_profile
        var subtitle: TextView = view.cardView_clubDesc_profile
        var cardview: MaterialCardView = view.cardView_detail_profile
    }

    private fun navigate(destination: Int, extra: Bundle? = null, view: View) {
        Navigation
            .findNavController(view)
            .navigate(destination, extra)
    }
}