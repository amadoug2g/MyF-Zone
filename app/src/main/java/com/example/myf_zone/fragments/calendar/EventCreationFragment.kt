package com.example.myf_zone.fragments.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myf_zone.R
import com.example.myf_zone.model.event.Event
import com.example.myf_zone.model.event.EventOwner
import com.example.myf_zone.util.event.EventUtil
import com.example.myf_zone.util.user.UserAccount.auth
import com.example.myf_zone.util.user.UserAccount.getCurrentClub
import com.example.myf_zone.util.user.UserAccount.getCurrentUser
import com.example.myf_zone.util.user.UserAffiliation
import kotlinx.android.synthetic.main.fragment_event_creation.*
import kotlinx.android.synthetic.main.fragment_event_creation.view.*
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
    private var param1: String? = null
    private var param2: String? = null

    private val currentUser = auth.currentUser

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
        fragmentInflater.event_day_picker.setOnClickListener {
            selectDate()
        }

        //Event Hour
        fragmentInflater.event_time_picker.setOnClickListener {
            selectTime()
        }

        //Event Creation
        fragmentInflater.event_create_button.setOnClickListener {
            if (currentUser != null) {
                UserAffiliation.userAffiliationStatus {
                    when (it) {
                        true -> {
                            toast("Creating event...")
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

        typeSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, selected ->
                val longVal: Long = 0
                if (selected == longVal) {
                    teamSpinner.isEnabled = false
                    teamSpinner.setSelection(0)
                } else {
                    teamSpinner.isEnabled = true
                }
            }
        }

        val day = Date().time
        val date = DateFormat.format("dd/MM/yyyy", Date()).toString()
        val time = DateFormat.format("HH:mm", Date()).toString()


//        val today = MaterialDatePicker.todayInUtcMilliseconds()
//
//        val builderDay = MaterialDatePicker.Builder.datePicker().setSelection(today).build()
//        val builderTime = MaterialTimePicker.Builder().apply {
//            setTimeFormat(TimeFormat.CLOCK_24H)
//            setHour(12)
//            setMinute(0)
//        }.build()
//
//        val pickerDay = builderDay.headerText
//        val pickerTime = "${builderTime.hour}:${builderTime.minute}"
//
        event_day_picker.text = date
        event_time_picker.text = time
    }

    fun createEvent() {
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
                    id
                    title = event_create_title_input.text.toString()
                    description = event_create_desc_input.text.toString()
                    type = getEventType()
                    nbTeam = teamSpinner.selectedItem.toString().toInt()
                    date
                    address
                    lat
                    lng
                    createdDate = Calendar.getInstance().time
                    owner = eventOwner
                }

                EventUtil.createEvent(newEvent)
            }
        }
    }

    private fun getEventType(): String {
        var result = "Error"
        when (typeSpinner.selectedItem.toString()) {
            getString(R.string.friendly_event) -> {
                result = "friendly"
            }
            getString(R.string.plateau_event) -> {
                result = "plateau"
            }
            getString(R.string.tournament_event) -> {
                result = "tournament"
            }
        }
        return result
    }

    private fun selectDate() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dateListener = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.YEAR, year)
                event_day_picker.text = SimpleDateFormat("dd/MM/Y", Locale.FRANCE).format(cal.time)
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
            event_time_picker.text = SimpleDateFormat("HH:mm", Locale.FRANCE).format(cal.time)
        }
        TimePickerDialog(
            requireContext(),
            timeListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
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

    private fun navigate(destination: Int) {
        findNavController().navigate(destination)
    }
}