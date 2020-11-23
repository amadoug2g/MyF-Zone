package mfz.myfzone_sport.myf_zone.fragments.maps

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.card_event_owner.*
import kotlinx.android.synthetic.main.card_event_participant.view.*
import kotlinx.android.synthetic.main.event_detail_cardview_map.*
import kotlinx.android.synthetic.main.event_detail_cardview_title.*
import kotlinx.android.synthetic.main.fragment_event_details_friendly.*
import kotlinx.android.synthetic.main.fragment_event_details_friendly.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.glide.GlideApp
import mfz.myfzone_sport.myf_zone.model.event.EventParticipant
import mfz.myfzone_sport.myf_zone.model.event.swipe_handler.ButtonClickListener
import mfz.myfzone_sport.myf_zone.model.event.swipe_handler.MyButton
import mfz.myfzone_sport.myf_zone.model.event.swipe_handler.SwipeHelper
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.checkUserParticipation
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.deleteEvent
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.getEventById
import mfz.myfzone_sport.myf_zone.util.event.OwnerUtil.acceptParticipant
import mfz.myfzone_sport.myf_zone.util.event.OwnerUtil.getOwnerFromEvent
import mfz.myfzone_sport.myf_zone.util.event.OwnerUtil.refuseParticipant
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.addParticipant
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.getParticipantsFromEvent
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.getValidParticipantCount
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.removeParticipant
import mfz.myfzone_sport.myf_zone.util.user.UserAccount
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.auth
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentUser
import mfz.myfzone_sport.myf_zone.util.user.UserAffiliation.affiliationStatus
import mfz.myfzone_sport.myf_zone.util.user.UserAffiliation.userAffiliationStatus
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

        val recyclerOptions = FirestoreRecyclerOptions.Builder<EventParticipant>()
            .setQuery(query, EventParticipant::class.java)
            .setLifecycleOwner(this)
            .build()

        adapter = object :
            FirestoreRecyclerAdapter<EventParticipant, ParticipantHolder>(recyclerOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_event_participant, parent, false)
//                    .inflate(R.layout.task_item, parent, false)
                return ParticipantHolder(view)
            }

            override fun onBindViewHolder(
                holder: ParticipantHolder,
                position: Int,
                model: EventParticipant
            ) {
                holder.bind(model)
            }

            override fun onDataChanged() {
                event_detail_empty_list.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
                val params = event_detail_participant_list.layoutParams
                params.height = 320 * itemCount
                event_detail_participant_list.layoutParams = params

                //Participate Btn visibility
                try {
                    userAffiliationStatus {
                        when (it) {
                            true -> {
                                getEventById(eventId!!) { event ->
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

    inner class ParticipantHolder(val view: View, var participant: EventParticipant? = null) :
        RecyclerView.ViewHolder(view) {

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
                        getEventById(eventId!!) { event ->
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
                    getEventById(eventId!!) { event ->
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
                                addParticipant(event.id, participant)
                            }
                        }

                    }
                }
                .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
                }.show()
        }

        return fragmentInflater
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.event_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, eventId.toString())

        event_detail_participant_list.setHasFixedSize(false)
        event_detail_participant_list.layoutManager = LinearLayoutManager(requireContext())
        event_detail_participant_list.adapter = adapter
        event_detail_participant_list.isNestedScrollingEnabled = false
//        val decor = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
//        event_detail_participant_list.addItemDecoration(decor)


        try {
            val formatEventDay = SimpleDateFormat("dd MMM Y", Locale.FRANCE)
            val formatEventHour = SimpleDateFormat("HH:mm", Locale.FRANCE)
            val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
            CoroutineScope(Main).launch {
                showProgressBar(event_detail_progressBar)
                getEventById(eventId!!) { event ->

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_event -> {
                val bundle = bundleOf("eventId" to eventId)
                navigate(R.id.eventDetailsToEventEdit, bundle)
            }

            R.id.delete_event -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.delete_event))
                    .setMessage(getString(R.string.delete_event_confirmation))
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton(getString(R.string.delete_txt)) { _: DialogInterface, _: Int ->
                        getEventById(eventId!!) { event ->
                            CoroutineScope(IO).launch {
                                deleteEvent(event.id)
                            }
                            requireActivity().onBackPressed()
                        }
                    }
                    .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
                    }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun View.snack(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(this, message, duration).show()
    }

    private suspend fun ownerFields(eventId: String) {
        val owner =
            getOwnerFromEvent(eventId)!!

        try {
            when (affiliationStatus()!!) {
                true -> {
                    getCurrentUser { coach ->
                        if (owner.coachId == coach.id) {
                            setHasOptionsMenu(true)
                            val swipe = object :
                                SwipeHelper(requireContext(), event_detail_participant_list, 250) {
                                override fun instantiateMyButton(
                                    viewHolder: RecyclerView.ViewHolder,
                                    buffer: MutableList<MyButton>
                                ) {
                                    buffer.add(
                                        MyButton(requireContext(),
                                            "Accept",
                                            50,
                                            R.drawable.ic_done,
                                            R.color.colorAccent,
                                            object :
                                                ButtonClickListener {
                                                override fun onClick(pos: Int) {
                                                    CoroutineScope(Main).launch {
                                                        val participants =
                                                            getParticipantsFromEvent(eventId)!!
                                                        val selected = participants[pos]
                                                        acceptParticipant(eventId, selected)
                                                        event_detail_imageView.snack("Accepted")
                                                    }
                                                }

                                            })
                                    )

                                    buffer.add(
                                        MyButton(requireContext(),
                                            "Refuse",
                                            50,
                                            R.drawable.ic_cancel,
                                            R.color.colorCoral,
                                            object :
                                                ButtonClickListener {
                                                override fun onClick(pos: Int) {
                                                    CoroutineScope(Main).launch {
                                                        val participants =
                                                            getParticipantsFromEvent(eventId)!!
                                                        val selected = participants[pos]
                                                        refuseParticipant(eventId, selected)
                                                    }
                                                }

                                            })
                                    )
                                }

                            }
                        } else {
                            setHasOptionsMenu(false)
                        }
                    }
                }
                false -> {

                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: $e")
        }


        val title =
            requireContext().getString(R.string.owner_txt) + " - " + owner.clubAcronym + " " + owner.categoryName + " " + owner.subCategoryName
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

    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }
}
