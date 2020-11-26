package mfz.myfzone_sport.myf_zone.fragments.maps

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_event_edit.*
import kotlinx.android.synthetic.main.fragment_event_edit.view.*
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.getEventById
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.updateEvent
import mfz.myfzone_sport.myf_zone.util.user.UserAccount
import mfz.myfzone_sport.myf_zone.util.user.UserAffiliation
import org.jetbrains.anko.sdk27.coroutines.onItemSelectedListener
import org.jetbrains.anko.support.v4.toast
import java.text.SimpleDateFormat
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "eventId"

class EventEditFragment : Fragment() {
    private val TAG = EventEditFragment::class.java.simpleName
    private var eventId: String? = null

    private val currentUser = UserAccount.auth.currentUser
    private var eventDay1: String? = null
    private var eventDay2: String? = null
    private var eventTime: String? = null
    private var eventDate: String? = null
    private var eventPlace: Place? = null
    private val AUTOCOMPLETE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater = inflater.inflate(R.layout.fragment_event_edit, container, false)

        //Event Date
        fragmentInflater.event_edit_day_picker.setOnClickListener {
            selectDate()
        }

        //Event Hour
        fragmentInflater.event_edit_time_picker.setOnClickListener {
            selectTime()
        }

        //Event Address
        Places.initialize(requireContext(), getString(R.string.google_maps_key))

        val fields = mutableListOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        fragmentInflater.event_edit_address_input.setOnClickListener {
            val intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).apply {
                    setHint(getString(R.string.enter_address_hint))
                }
                    .build(requireContext())
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        //Event Update
        fragmentInflater.event_edit_button.setOnClickListener {
            if (currentUser != null) {
                UserAffiliation.userAffiliationStatus {
                    when (it) {
                        true -> {
                            try {
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(getString(R.string.edit_event))
                                    .setMessage(getString(R.string.edit_event_confirmation))
                                    .setIcon(R.drawable.ic_info)
                                    .setPositiveButton(getString(R.string.edit_txt)) { _: DialogInterface, _: Int ->
                                        when (validateForm()) {
                                            true -> {
                                                updateEvent()
                                            }
                                            false -> {
                                            }
                                        }
                                    }
                                    .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
                                    }.show()

                            } catch (e: Exception) {
                                toast("Error on Edit: ${e.localizedMessage}")
                                Log.d(TAG, "Error: $e")
                            }
                        }
                        false -> {
                            toast(getString(R.string.new_event_error_msg))
                        }
                    }
                }
            } else {
                toast(getString(R.string.new_event_error_msg))
            }
        }

        return fragmentInflater
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        event_edit_typeSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, selected ->
                val longVal: Long = 0
                if (selected == longVal) {
                    event_edit_teamSpinner.isEnabled = false
                    event_edit_teamSpinner.setSelection(0)
                } else {
                    event_edit_teamSpinner.isEnabled = true
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        getEventById(eventId!!) { event ->
            event_edit_title_input.setText(event.title)
            event_edit_desc_input.setText(event.description)

            try {
                event_edit_typeSpinner.setSelection(getEventTypeToDisplay(event.eventTypeString))
            } catch (e: Exception) {
                toast("Error in selecting the type of event: $e")
            }

            try {
                event_edit_teamSpinner.setSelection(getEventNbTeamToDisplay(event.nbTeam))
            } catch (e: Exception) {
                toast("Error in selecting the number of participant: $e")
            }


            val date = DateFormat.format("dd/MM/yyyy", event.date)
            val time = DateFormat.format("HH:mm", event.date)

            event_edit_day_picker.text = date.toString()
            event_edit_time_picker.text = time.toString()

            val formatEventDay1 = SimpleDateFormat("E MMM dd", Locale.ENGLISH)
            val formatEventDay2 = SimpleDateFormat("z yyyy", Locale.ENGLISH)
            val formatEventHour = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
            val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

            eventDay1 = formatEventDay1.format(formatDate.parse(event.date.toString())!!)
            eventDay2 = formatEventDay2.format(formatDate.parse(event.date.toString())!!)
            eventTime = formatEventHour.format(formatDate.parse(event.date.toString())!!)

            event_edit_address_input.setText(event.address)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                if (data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    event_edit_address_input.setText(place.address)
                    eventPlace = place
                }
            }
        }
    }

    private fun getEventDate(): Date? {
        val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        return try {
            eventDate = "$eventDay1 $eventTime $eventDay2"
            val result = formatDate.parse(eventDate!!)
            result
        } catch (e: Exception) {
            toast("Error when getting the date: $e")
            null
        }
    }

    private fun updateEvent() {
        getEventById(eventId!!) { event ->
            val updatedEvent = when (eventPlace == null) {
                true -> {
                    val updatedEvent = Event().apply {
                        title = event_edit_title_input.text.toString()
                        description = event_edit_desc_input.text.toString()
                        type = getEventType(event_edit_typeSpinner.selectedItem.toString())
                        nbTeam = event_edit_teamSpinner.selectedItem.toString().toInt()
                        date = getEventDate()!!
                        address = event.address
                        lat = event.lat
                        lng = event.lng
                    }
                    updatedEvent
                }
                false -> {
                    val updatedEvent = Event().apply {
                        title = event_edit_title_input.text.toString()
                        description = event_edit_desc_input.text.toString()
                        type = getEventType(event_edit_typeSpinner.selectedItem.toString())
                        nbTeam = event_edit_teamSpinner.selectedItem.toString().toInt()
                        date = getEventDate()!!
                        address = eventPlace!!.address.toString()
                        lat = eventPlace!!.latLng!!.latitude
                        lng = eventPlace!!.latLng!!.longitude
                    }
                    updatedEvent
                }
            }

            Log.d(TAG, "Updated Event: $updatedEvent")
            try {
                updateEvent(eventId!!, updatedEvent)
                toast("Event successfully updated")
                requireActivity().onBackPressed()
            } catch (e: Exception) {
                toast("Error when updating the event: $e")
            }
        }
    }

    private fun getEventTypeToDisplay(type: String): Int {
        return when (type) {
            /*getString(R.string.friendly_event)*/ "Match Amical" -> {
                0
            }
            /*getString(R.string.plateau_event)*/ "Plateau" -> {
                1
            }
            /*getString(R.string.tournament_event)*/ "Tournoi" -> {
                2
            }
            else -> {
                toast("Error on the type of this Event")
                -1
            }
        }
    }

    private fun getEventNbTeamToDisplay(nbTeam: Int): Int {
        return (nbTeam - 2)
    }

    private fun getEventType(type: String): String {
        return when (type) {
            /*getString(R.string.friendly_event)*/ "Match Amical" -> {
                "friendly"
            }
            /*getString(R.string.plateau_event)*/ "Plateau" -> {
                "plateau"
            }
            /*getString(R.string.tournament_event)*/ "Tournoi" -> {
                "tournament"
            }
            else -> {
                "Error in getEventType"
            }
        }
    }

    private fun selectDate() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dateListener = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, y, m, d ->
                cal.set(Calendar.DAY_OF_MONTH, d)
                cal.set(Calendar.MONTH, m)
                cal.set(Calendar.YEAR, y)
                event_edit_day_picker.text =
                    SimpleDateFormat("dd/MM/Y", Locale.FRANCE).format(cal.time)
                eventDay1 = SimpleDateFormat("E MMM dd", Locale.ENGLISH).format(cal.time)
                eventDay2 = SimpleDateFormat("z yyyy", Locale.ENGLISH).format(cal.time)
            },
            year,
            month,
            day
        )
        dateListener.datePicker.minDate = cal.timeInMillis - 1000
        dateListener.show()
    }

    private fun selectTime() {
        val cal = Calendar.getInstance()
        val timeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            event_edit_time_picker.text =
                SimpleDateFormat("HH:mm", Locale.FRANCE).format(cal.time)
            eventTime = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(cal.time)
        }
        TimePickerDialog(
            requireContext(),
            timeListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun validateForm(): Boolean {
        var valid = true

        val title = event_edit_title_input.text.toString()
        val description = event_edit_desc_input.text.toString()
        val address = event_edit_address_input.text.toString()

        when {
            TextUtils.isEmpty(title.trim()) -> {
                event_edit_title_layout.error = getString(R.string.hint_required)
                valid = false
            }
            title.trim().length > 21 -> {
                event_edit_title_layout.error = getString(R.string.hint_required)
                valid = false
            }
            else -> {
                event_edit_title_layout.error = null
            }
        }

        when {
            TextUtils.isEmpty(description.trim()) -> {
                event_edit_desc_layout.error = getString(R.string.hint_required)
                valid = false
            }
            description.trim().length > 51 -> {
                event_edit_desc_layout.error = getString(R.string.hint_required)
                valid = false
            }
            else -> {
                event_edit_desc_layout.error = null
            }
        }

        if (TextUtils.isEmpty(address.trim())) {
            event_edit_address_layout.error = getString(R.string.hint_required)
            valid = false
        } else {
            event_edit_address_layout.error = null
        }

        if (getEventDate() !is Date) {
            toast(getString(R.string.date_required))
            valid = false
        }

        return valid
    }
}