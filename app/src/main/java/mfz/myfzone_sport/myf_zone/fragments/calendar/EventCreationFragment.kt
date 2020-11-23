package mfz.myfzone_sport.myf_zone.fragments.calendar

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import kotlinx.android.synthetic.main.fragment_event_creation.*
import kotlinx.android.synthetic.main.fragment_event_creation.view.*
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventOwner
import mfz.myfzone_sport.myf_zone.util.event.EventUtil
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.auth
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentClub
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentUser
import mfz.myfzone_sport.myf_zone.util.user.UserAffiliation
import org.jetbrains.anko.sdk27.coroutines.onItemSelectedListener
import org.jetbrains.anko.support.v4.toast
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventCreationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventCreationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private val TAG = EventCreationFragment::class.java.simpleName
    private var param1: String? = null
    private var param2: String? = null

    private val currentUser = auth.currentUser
    private var eventDay1: String? = null
    private var eventDay2: String? = null
    private var eventTime: String? = null
    private var eventDate: String? = null
    private var eventPlace: Place? = null
    private val AUTOCOMPLETE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater = inflater.inflate(R.layout.fragment_event_creation, container, false)

        //Event Date
        fragmentInflater.event_create_day_picker.setOnClickListener {
            selectDate()
        }

        //Event Hour
        fragmentInflater.event_create_time_picker.setOnClickListener {
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

        fragmentInflater.event_create_address_input.setOnClickListener {
            val intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).apply {
                    setHint(getString(R.string.enter_address_hint))
                }
                    .build(requireContext())
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        //Event Creation
        fragmentInflater.event_create_button.setOnClickListener {
            if (currentUser != null) {
                UserAffiliation.userAffiliationStatus {
                    when (it) {
                        true -> {
                            try {
                                when (validateForm()) {
                                    true -> {
                                        createEvent()
                                    }
                                    false -> {
                                    }
                                }
                            } catch (e: Exception) {
                                toast("Error: $e")
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

        event_create_typeSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, selected ->
                val longVal: Long = 0
                if (selected == longVal) {
                    event_create_teamSpinner.isEnabled = false
                    event_create_teamSpinner.setSelection(0)
                } else {
                    event_create_teamSpinner.isEnabled = true
                }
            }
        }

        val date = DateFormat.format("dd/MM/yyyy", Date()).toString()
        val time = DateFormat.format("HH:mm", Date()).toString()

        event_create_day_picker.text = date
        event_create_time_picker.text = time
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                if (data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    event_create_address_input.setText(place.address)
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
            null
        }
    }

    private fun createEvent() {
        getCurrentUser { coach ->
            getCurrentClub { club ->
                val eventOwner = EventOwner().apply {
                    clubLogo = club.clubLogo
                    clubAcronym = club.clubAcronym
                    coachId = coach.id
                    coachFullname = coach.getName()
                    sportId = club.sportId
                    sportName = club.sportName
                    if (!club.categoryId.isNullOrEmpty() && !club.categoryName.isNullOrEmpty()) {
                        categoryName = club.categoryName!!
                        categoryId = club.categoryId!!
                        if (!club.subCategoryId.isNullOrEmpty() && !club.subCategoryName.isNullOrEmpty()) {
                            subCategoryId = club.subCategoryId!!
                            subCategoryName = club.subCategoryName!!
                        }
                    }
                }

                val newEvent = Event().apply {
                    title = event_create_title_input.text.toString()
                    description = event_create_desc_input.text.toString()
                    type = getEventType(event_create_typeSpinner.selectedItem.toString())
                    nbTeam = event_create_teamSpinner.selectedItem.toString().toInt()
                    date = getEventDate()!!
                    address = eventPlace!!.address.toString()
                    lat = eventPlace!!.latLng!!.latitude
                    lng = eventPlace!!.latLng!!.longitude
                    createdDate = Calendar.getInstance().time
                    owner = eventOwner
                }

                Log.d(TAG, "New Event: $newEvent")
                try {
                    EventUtil.createEvent(newEvent, eventOwner)
                    toast("Event successfully created")
                    requireActivity().onBackPressed()
                } catch (e: Exception) {
                    toast("Error: $e")
                }
            }
        }
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
                "Error"
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
            DatePickerDialog.OnDateSetListener { datePicker, y, m, d ->
                cal.set(Calendar.DAY_OF_MONTH, d)
                cal.set(Calendar.MONTH, m)
                cal.set(Calendar.YEAR, y)
                event_create_day_picker.text =
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
        val timeListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            event_create_time_picker.text =
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

        val title = event_create_title_input.text.toString()
        val description = event_create_desc_input.text.toString()
        val address = event_create_address_input.text.toString()

        when {
            TextUtils.isEmpty(title.trim()) -> {
                event_create_title_layout.error = getString(R.string.hint_required)
                valid = false
            }
            title.trim().length > 21 -> {
                event_create_title_layout.error = getString(R.string.hint_required)
                valid = false
            }
            else -> {
                event_create_title_layout.error = null
            }
        }

        when {
            TextUtils.isEmpty(description.trim()) -> {
                event_create_desc_layout.error = getString(R.string.hint_required)
                valid = false
            }
            description.trim().length > 51 -> {
                event_create_desc_layout.error = getString(R.string.hint_required)
                valid = false
            }
            else -> {
                event_create_desc_layout.error = null
            }
        }

        if (TextUtils.isEmpty(address.trim())) {
            event_create_address_layout.error = getString(R.string.hint_required)
            valid = false
        } else {
            event_create_address_layout.error = null
        }

        if (getEventDate() !is Date) {
            toast(getString(R.string.date_required))
            valid = false
        }

        return valid
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventCreationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventCreationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}