package com.example.myf_zone.fragments.maps

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myf_zone.R
import com.example.myf_zone.glide.GlideApp
import com.example.myf_zone.model.event.EventParticipant
import com.example.myf_zone.util.Constants.EVENT_PATH
import com.example.myf_zone.util.event.EventUtil
import com.example.myf_zone.util.event.EventUtil.checkUserParticipation
import com.example.myf_zone.util.event.EventUtil.getEventFromId
import com.example.myf_zone.util.event.EventUtil.getOwnerFromEvent
import com.example.myf_zone.util.event.EventUtil.getValidParticipantCount
import com.example.myf_zone.util.event.EventUtil.removeParticipant
import com.example.myf_zone.util.user.UserAccount
import com.example.myf_zone.util.user.UserAccount.auth
import com.example.myf_zone.util.user.UserAccount.getCurrentUser
import com.example.myf_zone.util.user.UserAffiliation.userAffiliationStatus
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.android.synthetic.main.card_event_owner.*
import kotlinx.android.synthetic.main.card_event_participant.view.*
import kotlinx.android.synthetic.main.event_detail_cardview_map.*
import kotlinx.android.synthetic.main.event_detail_cardview_title.*
import kotlinx.android.synthetic.main.fragment_event_details_friendly.*
import kotlinx.android.synthetic.main.fragment_event_details_friendly.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.toast
import java.text.SimpleDateFormat
import java.util.*


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "eventId"

class EventDetailsFragment : Fragment() {
    private val TAG = EventDetailsFragment::class.java.simpleName
    private var eventId: String? = null
    private var adapter: FirestoreRecyclerAdapter<EventParticipant, ParticipantHolder>? = null

    private val currentUser = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }

        val instance = FirebaseFirestore.getInstance()

        val query = instance
            .collection(EVENT_PATH)
            .document(eventId!!)
            .collection("Participant")

        query.get().addOnSuccessListener {
            for (item in it.documents) {
                Log.d(TAG, "Success: ${item.toObject<EventParticipant>()}")
            }
        }.addOnCompleteListener {
            Log.d(TAG, "Complete: $it")
        }.addOnFailureListener {
            Log.d(TAG, "Failure: $it")
        }

        Log.d(TAG, "path is ${query.path}")

        val recyclerOptions = FirestoreRecyclerOptions.Builder<EventParticipant>()
            .setQuery(query, EventParticipant::class.java)
            .setLifecycleOwner(this)
            .build()

        adapter = object :
            FirestoreRecyclerAdapter<EventParticipant, ParticipantHolder>(recyclerOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_event_participant, parent, false)
                return ParticipantHolder(view)
            }

            override fun onBindViewHolder(
                holder: ParticipantHolder,
                position: Int,
                model: EventParticipant
            ) {
//                val participate = holder.itemView.event_detail_owner_club.context.getString(R.string.participate_txt)
//                val participantTitle = "$participate - ${model.clubAcronym} ${model.categoryName} ${model.subCategoryName}"
//
//                holder.textViewClub.text = participantTitle
//                holder.itemView.event_detail_owner_name.text = model.coachFullname

                holder.bind(model)
            }

            override fun onDataChanged() {
                // If there are no chat messages, show a view that invites the user to add a message.
                event_detail_empty_list.visibility = if (itemCount == 0) View.VISIBLE else View.GONE

                //Participate Btn visibility
                try {
                    userAffiliationStatus {
                        when (it) {
                            true -> {
                                getEventFromId(eventId!!) { event ->
                                    CoroutineScope(Main).launch {
                                        if (checkUserParticipation(event.id)!!) {
                                            participateButton.visibility = View.GONE
                                        } else {
                                            participateButton.visibility = View.VISIBLE
                                        }
                                    }
                                }
                            }
                            false -> {
                                toast("Vous n'êtes pas affilié")
                                participateButton.visibility = View.GONE
                            }
                        }
                    }
                } catch (e: Exception) {
                    toast("Error: $e")
                }
            }
        }
    }

    inner class ParticipantHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(participant: EventParticipant) {
            with(participant) {

                try {
                    GlideApp.with(view.context).apply {
                        load(UserAccount.pathToReference(participant.clubLogo))
                            .placeholder(R.drawable.ic_account)
                            .centerCrop()
                            .into(view.event_detail_participant_image)
                    }
                } catch (e: Exception) {
                    Log.e("ParticipantAdapter", "Image could not load: $e")
                }

                try {
                    var dotBg: Int = R.drawable.notification_dot_blue
                    when (participant.status) {
                        "pending" -> dotBg = R.drawable.notification_dot_blue
                        "validate" -> dotBg = R.drawable.notification_dot_green
                        "refused" -> dotBg = R.drawable.notification_dot_red
                    }
                    view.notificationDotOwner.setImageResource(dotBg)
                } catch (e: Exception) {
                    Log.e("ParticipantAdapter", "Image could not load: $e")
                }

                when (participant.coachId) {
                    currentUser?.uid -> {
                        view.cancel_participation.visibility = View.VISIBLE

                        try {
                            view.cancel_participation.setOnClickListener {

                                MaterialAlertDialogBuilder(view.context)
                                    .setTitle(view.context.getString(R.string.event_exit))
                                    .setMessage(view.context.getString(R.string.exit_event_msg))
                                    .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                                        removeParticipant(eventId!!)
                                    }
                                    .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
                                    }.show()
                            }
                        } catch (e: Exception) {
                            Log.d("ParticipantAdapter", "Could not leave event: $e")
                        }
                    }
                    else -> view.cancel_participation.visibility = View.GONE
                }

                val participate =
                    view.event_detail_owner_club.context.getString(R.string.participate_txt)
                val participantTitle =
                    "$participate - ${participant.clubAcronym} ${participant.categoryName} ${participant.subCategoryName}"

                view.event_detail_owner_club.text = participantTitle
                view.event_detail_owner_name.text = participant.coachFullname
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val fragmentInflater =
            inflater.inflate(R.layout.fragment_event_details_friendly, container, false)

        //Participate Btn visibility
        try {
            userAffiliationStatus {
                when (it) {
                    true -> {
                        getEventFromId(eventId!!) { event ->
                            CoroutineScope(Main).launch {
                                if (checkUserParticipation(event.id)!!) {
                                    participateButton.visibility = View.GONE
                                } else {
                                    participateButton.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                    false -> {
                        toast("Vous n'êtes pas affilié")
                        participateButton.visibility = View.GONE
                    }
                }
            }
        } catch (e: Exception) {
            toast("Error: $e")
        }

        fragmentInflater.participateButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.enter_event))
                .setMessage(getString(R.string.enter_event_msg))
                .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                    participateButton.visibility = View.GONE
                    getEventFromId(eventId!!) { event ->
                        UserAccount.getCurrentClub { affiliation ->
                            getCurrentUser { user ->
                                val participant = EventParticipant().apply {
                                    clubLogo = affiliation.clubLogo
                                    clubAcronym = affiliation.clubAcronym
                                    coachId = user.id
                                    coachFullname = "${user.firstName} ${user.lastName}"
                                    sportId = affiliation.sportId
                                    sportName = affiliation.sportName
                                    if (!affiliation.categoryId.isNullOrEmpty()) {
                                        categoryId = affiliation.categoryId
                                        categoryName = affiliation.categoryName
                                        if (!affiliation.subCategoryId.isNullOrEmpty()) {
                                            subCategoryId = affiliation.subCategoryId
                                            subCategoryName = affiliation.subCategoryName
                                        }
                                    }
                                    status = "pending"
                                }
                                EventUtil.addParticipant(event.id, participant)
                            }
                        }

                    }
                }
                .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
                }.show()
        }

        return fragmentInflater
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, eventId.toString())

        event_detail_participant_list.setHasFixedSize(false)
        event_detail_participant_list.layoutManager = LinearLayoutManager(requireContext())
        event_detail_participant_list.adapter = adapter
//        val decor = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
//        event_detail_participant_list.addItemDecoration(decor)


        try {
            val formatEventDay = SimpleDateFormat("dd MMM Y", Locale.FRANCE)
            val formatEventHour = SimpleDateFormat("HH:mm", Locale.FRANCE)
            val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
            CoroutineScope(Main).launch {
                showProgressBar(event_detail_progressBar)
                getEventFromId(eventId!!) { event ->

                    //Event Details
                    val eventDay = formatEventDay.format(formatDate.parse(event.date.toString())!!)
                    val eventHour =
                        formatEventHour.format(formatDate.parse(event.date.toString())!!)
                    event_detail_address.text = event.address
                    event_detail_day.text = eventDay
                    event_detail_hour.text = eventHour
                    event_detail_type.text = event.eventTypeString
                    event_detail_title.text = event.title
                    event_detail_description.text = event.description
                    event_detail_title.text = event.title
                    event_detail_team.text = event.nbTeam.toString().plus(getString(R.string.teams))
                    event_detail_imageView.setImageResource(event.eventTypeImage)

                    CoroutineScope(Main).launch {
                        val participantCpt = getValidParticipantCount(event.id) ?: "?"
                        val teamCpt = "$participantCpt/${event.nbTeam}"
                        event_detail_nbTeam.text = teamCpt
                    }


                    //Event Owner
                    CoroutineScope(Main).launch {
                        ownerFields(event.id)
                    }

                    //Page Title
                    (activity as AppCompatActivity).supportActionBar?.apply {
                        title = event.eventTypeString
                    }

                    //MapView
                    event_detail_map.onCreate(savedInstanceState)
                    event_detail_map.onResume()

                    try {
                        MapsInitializer.initialize(context)
                    } catch (e: Exception) {
                        Log.d(TAG, "Error: $e")
                    }

                    event_detail_map.getMapAsync { map ->
                        val markerOptions = MarkerOptions().apply {
                            position(event.getPosition())
                            snippet(event.address)
                        }

                        map.uiSettings.apply {
                            setAllGesturesEnabled(false)
                            isZoomControlsEnabled = false
                            isRotateGesturesEnabled = false
                            isScrollGesturesEnabled = false
                            isScrollGesturesEnabledDuringRotateOrZoom = false
                            isZoomControlsEnabled = false
                            isTiltGesturesEnabled = false
                        }
                        map.addMarker(markerOptions)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(event.getPosition(), 14f))
                        map.setOnMapClickListener {
                            showMap(event.address)
                        }
                        map.setOnMarkerClickListener {
                            true
                        }
                    }
                }
                hideProgressBar(event_detail_progressBar)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: $e")
        }

        hideProgressBar(event_detail_progressBar)
    }

    override fun onPause() {
        super.onPause()
        event_detail_map.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        event_detail_map.onLowMemory()
    }

    private suspend fun ownerFields(eventId: String) {
        val owner =
            getOwnerFromEvent(eventId)!!

        val title =
            "Organisateur - " + owner.clubAcronym + " " + owner.categoryName + " " + owner.subCategoryName
        event_detail_owner_club.text = title
        event_detail_owner_name.text = owner.coachFullname

        try {
            GlideApp.with(this).apply {
                load(UserAccount.pathToReference(owner.clubLogo))
                    .placeholder(R.drawable.ic_account)
                    .centerCrop()
                    .into(event_detail_owner_image)
            }
        } catch (e: Exception) {
            toast("Image could not load: $e")
        }
    }

    private fun showMap(position: String) {
        val uri =
            java.lang.String.format(
                Locale.FRANCE,
                "geo:0,0?q=$position"
            )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        requireContext().startActivity(intent)
    }

    private fun showProgressBar(progressBar: ProgressBar) {
        progressBar.apply {
            visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar(progressBar: ProgressBar) {
        progressBar.apply {
            visibility = View.GONE
        }
    }
}
