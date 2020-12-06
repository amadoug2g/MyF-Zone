package mfz.myfzone_sport.myf_zone.fragments.event_details

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
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
import kotlinx.android.synthetic.main.card_event_owner.*
import kotlinx.android.synthetic.main.card_event_participant.view.*
import kotlinx.android.synthetic.main.event_detail_cardview_map.*
import kotlinx.android.synthetic.main.event_detail_cardview_title.*
import kotlinx.android.synthetic.main.fragment_event_details.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.CardEventParticipantBinding
import mfz.myfzone_sport.myf_zone.databinding.FragmentEventDetailsBinding
import mfz.myfzone_sport.myf_zone.fragments.event_details.EventDetailsService.getEvent
import mfz.myfzone_sport.myf_zone.fragments.event_details.EventDetailsService.getImageReference
import mfz.myfzone_sport.myf_zone.glide.GlideApp
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventParticipant
import mfz.myfzone_sport.myf_zone.model.event.swipe_handler.ButtonClickListener
import mfz.myfzone_sport.myf_zone.model.event.swipe_handler.MyButton
import mfz.myfzone_sport.myf_zone.model.event.swipe_handler.SwipeHelper
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.getEventById
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.addParticipant
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.getValidParticipantCount
import mfz.myfzone_sport.myf_zone.util.user.UserAccount
import org.jetbrains.anko.support.v4.toast
import java.util.*


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "eventId"

class EventDetailsFragment : Fragment() {
    companion object {
        private val TAG = EventDetailsFragment::class.java.simpleName
        private var eventId: String? = null
        private var adapter: FirestoreRecyclerAdapter<EventParticipant, ParticipantHolder>? = null

        private lateinit var binding: FragmentEventDetailsBinding
        private lateinit var viewModel: EventDetailsViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }

        viewModel = ViewModelProvider(this).get(EventDetailsViewModel::class.java)

        viewModel.eventId.value = eventId!!

        lifecycleScope.launch {
            viewModel.assignEvent()
            viewModel.assignClub() //TODO: necessary?
            viewModel.assignOwner()
            viewModel.assignParticipants()
            viewModel.checkUserAffiliationStatus()
            viewModel.checkParticipationStatus()
//            viewModel.checkIsUserOwner()
        }

        val recyclerOptions = FirestoreRecyclerOptions.Builder<EventParticipant>()
            .setQuery(viewModel.getQuery(eventId!!), EventParticipant::class.java)
            .setLifecycleOwner(this)
            .build()

        adapter = object :
            FirestoreRecyclerAdapter<EventParticipant, ParticipantHolder>(recyclerOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantHolder {
                return ParticipantHolder.from(parent)
            }

            override fun onBindViewHolder(
                holder: ParticipantHolder,
                position: Int,
                model: EventParticipant
            ) {
                holder.bind(model)
            }

            override fun onDataChanged() {

                lifecycleScope.launch {
                    viewModel.checkParticipationStatus()
                }

                binding.eventDetailEmptyList.visibility =
                    if (itemCount == 0) View.VISIBLE else View.GONE

                val params = binding.eventDetailParticipantList.layoutParams
                params.height = 320 * itemCount
                binding.eventDetailParticipantList.layoutParams = params

                try {
                    getEventById(eventId!!) { event ->
                        lifecycleScope.launch {
                            val participantCpt = getValidParticipantCount(event.id) ?: "?"
                            val teamCpt = "$participantCpt/${event.nbTeam}"
                            binding.eventDetailNbTeam.text = teamCpt
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error [onDataChanged] participant count: $e")
                }

                viewModel.isUserSignedIn.observe(viewLifecycleOwner) { isUserSignedIn ->
                    if (isUserSignedIn) {
                        viewModel.isUserAffiliated.observe(viewLifecycleOwner) { isUserAffiliated ->
                            if (isUserAffiliated) {
                                viewModel.isUserOwner.observe(viewLifecycleOwner) { isUserOwner ->
                                    if (isUserOwner) {
//                                        ownerAdmin(isUserOwner)
                                        binding.participateButton.visibility = View.GONE
                                    }
                                }

                            }
                        }
                    }
                }
                lifecycleScope.launch {
                    viewModel.checkParticipationStatus()
                }
            }

            suspend fun getChangedEvent() {
                getEvent(eventId!!).collect { state ->
                    when (state) {
                        is State.Loading -> {

                        }
                        is State.Success -> {

                        }
                        is State.Failed -> {

                        }
                    }
                }
            }
        }
    }

    class ParticipantHolder(val binding: CardEventParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: EventParticipant) {
            with(binding) {
                binding.participant = participant

                try {
                    GlideApp.with(itemView).apply {
                        load(UserAccount.pathToReference(participant.clubLogo))
                            .placeholder(R.drawable.ic_account)
                            .centerCrop()
                            .into(binding.eventDetailParticipantImage)
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
                    binding.notificationDotOwner.setImageResource(dotBg)
                } catch (e: Exception) {
                    Log.e("ParticipantAdapter", "Image could not load: $e")
                }

//                viewModel.isUserOwner

                viewModel.checkIsUserParticipant(participant)

                binding.cancelParticipation.visibility =
                    if (viewModel.isUserParticipant.value!!) View.VISIBLE else View.GONE

                binding.cancelParticipation.setOnClickListener { cancelParticipation() }
            }
        }

        private fun cancelParticipation() {
            MaterialAlertDialogBuilder(itemView.context)
                .setTitle(itemView.context.getString(R.string.event_exit))
                .setMessage(itemView.context.getString(R.string.exit_event_msg))
                .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                    viewModel.removeParticipant(viewModel.eventId.value!!)
                    EventDetailsFragment.binding.participateButton.visibility = View.VISIBLE
                }
                .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
                }.show()
        }

        companion object {
            fun from(parent: ViewGroup): ParticipantHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CardEventParticipantBinding.inflate(layoutInflater, parent, false)
                return ParticipantHolder(binding)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_event_details,
            container,
            false
        )

        lifecycleScope.launch {
            assignOwner()
            assignEvent(savedInstanceState)
        }

        binding.apply {
            lifecycleOwner = this@EventDetailsFragment
            setupRecyclerParameters()
            executePendingBindings()
        }

        lifecycleScope.launch {
            viewModel.checkParticipationStatus()
        }

        viewModel.owner.observe(viewLifecycleOwner) { owner ->
            binding.owner = owner
            try {
                GlideApp.with(requireContext()).apply {
                    load(getImageReference(owner.clubLogo))
                        .placeholder(R.drawable.ic_account)
                        .into(event_detail_owner_image)
                }
            } catch (e: Exception) {
                toast("Owner Image could not load: $e")
            }
        }

        viewModel.isUserOwner.observe(viewLifecycleOwner) { isUserOwner ->
            ownerAdmin(isUserOwner)
        }

        binding.participateButton.setOnClickListener {
            participationWindow()
        }

        return binding.root
    }

    private fun confirmParticipation() {
        val participant = EventParticipant().apply {
            clubLogo = viewModel.club.value!!.clubLogo
            clubAcronym = viewModel.club.value!!.clubAcronym
            coachId = viewModel.coach.value!!.id
            coachFullname =
                "${viewModel.coach.value!!.firstName} ${viewModel.coach.value!!.lastName}"
            sportId = viewModel.club.value!!.sportId
            sportName = viewModel.club.value!!.sportName
            if (!viewModel.club.value!!.categoryId.isNullOrEmpty()) {
                categoryId = viewModel.club.value!!.categoryId
                categoryName = viewModel.club.value!!.categoryName
                if (!viewModel.club.value!!.subCategoryId.isNullOrEmpty()) {
                    subCategoryId = viewModel.club.value!!.subCategoryId
                    subCategoryName = viewModel.club.value!!.subCategoryName
                }
            }
            status = "pending"
        }

        lifecycleScope.launch {
            addParticipant(participant)
        }

        addParticipant(eventId!!, participant)
    }

    private fun participationWindow() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.enter_event))
            .setMessage(getString(R.string.enter_event_msg))
            .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                confirmParticipation()
            }
            .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
            }.show()
    }

    private fun confirmDeletion() {
        viewModel.deleteEvent(viewModel.eventId.value!!, viewModel.club.value!!)
        requireActivity().onBackPressed()
    }

    private fun deletionWindow() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_event))
            .setMessage(getString(R.string.delete_event_confirmation))
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton(getString(R.string.delete_txt)) { _: DialogInterface, _: Int ->
                confirmDeletion()
            }
            .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
            }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.event_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_event -> {
                val bundle = bundleOf("eventId" to eventId)
                navigate(R.id.eventDetailsToEventEdit, bundle)
            }

            R.id.delete_event -> {
                deletionWindow()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun View.snack(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        val msg = Snackbar.make(this, message, duration)
        msg.show()
    }

    private fun setupRecyclerParameters() {
        binding.eventDetailParticipantList.setHasFixedSize(false)
        binding.eventDetailParticipantList.layoutManager = LinearLayoutManager(requireContext())
        binding.eventDetailParticipantList.adapter = adapter
        binding.eventDetailParticipantList.isNestedScrollingEnabled = false
    }

    private suspend fun participantCount(event: Event) {
        val participantCpt = getValidParticipantCount(event.id) ?: "?"
        val teamCpt = "$participantCpt/${event.nbTeam}"
        binding.eventDetailNbTeam.text = teamCpt
    }

    private suspend fun addParticipant(participant: EventParticipant) {
        viewModel.addParticipant(viewModel.eventId.value!!, participant).collect { state ->
            when (state) {
                is State.Loading -> {

                }
                is State.Success -> {

                }
                is State.Failed -> {

                }
            }
        }
    }

    private suspend fun acceptParticipant(participant: EventParticipant) {
        viewModel.acceptParticipant(viewModel.eventId.value!!, participant).collect { state ->
            when (state) {
                is State.Loading -> {

                }
                is State.Success -> {

                }
                is State.Failed -> {

                }
            }
        }
    }

    private suspend fun refuseParticipant(participant: EventParticipant) {
        viewModel.refuseParticipant(viewModel.eventId.value!!, participant).collect { state ->
            when (state) {
                is State.Loading -> {

                }
                is State.Success -> {

                }
                is State.Failed -> {

                }
            }
        }
    }

    private suspend fun assignOwner() {
        viewModel.getOwnerFromEvent(viewModel.eventId.value!!).collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    val owner = state.data
                    binding.owner = owner
                    try {
                        GlideApp.with(event_detail_owner_image.context).apply {
                            load(getImageReference(owner.clubLogo))
                                .placeholder(R.drawable.ic_account)
                                .centerCrop()
                                .into(event_detail_owner_image)
                        }
                    } catch (e: Exception) {
                        toast("Owner Image could not load: $e")
                    }
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    showToast(message)
                }
            }
        }
    }

    private suspend fun assignEvent(savedInstanceState: Bundle?) {
        viewModel.getEvent(viewModel.eventId.value!!).collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    val event = state.data
                    binding.event = event
                    binding.viewModel = viewModel

                    //Page Title
                    (activity as AppCompatActivity).supportActionBar?.apply {
                        title = event.eventTypeString
                    }

                    //Event Map
                    mapView(event, savedInstanceState)

                    lifecycleScope.launch {
                        participantCount(event)
                    }
                    hideProgressBar()
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    showToast(message)
                }
            }
        }
    }

    private fun ownerAdmin(isUserOwner: Boolean) {
        if (isUserOwner) {
            setHasOptionsMenu(true)
            val swipe = object :
                SwipeHelper(
                    requireActivity(),
                    binding.eventDetailParticipantList,
                    250
                ) {
                override fun instantiateMyButton(
                    viewHolder: RecyclerView.ViewHolder,
                    buffer: MutableList<MyButton>
                ) {
                    buffer.add(
                        MyButton(
                            requireContext(),
                            "Accept",
                            50,
                            R.drawable.ic_done,
                            R.color.colorAccent,
                            object :
                                ButtonClickListener {
                                override fun onClick(pos: Int) {
                                    lifecycleScope.launch {
                                        acceptParticipant(viewModel.participantList.value!![pos])
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
                                    lifecycleScope.launch {
                                        refuseParticipant(viewModel.participantList.value!![pos])
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

    private fun mapView(event: Event, savedInstanceState: Bundle?) {
        event_detail_map.onCreate(savedInstanceState)
        event_detail_map.onResume()

        try {
            MapsInitializer.initialize(context)
        } catch (e: Exception) {
            Log.d(TAG, "Error in mapView: $e")
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
                redirectToMap(event.address)
            }
            map.setOnMarkerClickListener {
                true
            }
        }
    }

    private fun redirectToMap(position: String) {
        val uri =
            java.lang.String.format(
                Locale.FRANCE,
                "geo:0,0?q=$position"
            )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        requireContext().startActivity(intent)
    }

    private fun showProgressBar() {
//        binding.eventDetailProgressBar.apply {
//            visibility = View.VISIBLE
//        }

        binding.profileShimmerLayout.startShimmer()
        binding.profileShimmerLayout.visibility = View.VISIBLE
        binding.profileLayout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        binding.profileShimmerLayout.startShimmer()
    }

    override fun onStop() {
        super.onStop()
        binding.profileShimmerLayout.stopShimmer()
    }

    private fun hideProgressBar() {
//        binding.eventDetailProgressBar.apply {
//            visibility = View.GONE
//        }

        binding.profileShimmerLayout.stopShimmer()
        binding.profileShimmerLayout.visibility = View.GONE
        binding.profileLayout.visibility = View.VISIBLE
    }

    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }

    private fun showToast(string: String) {
        toast(string)
    }
}
