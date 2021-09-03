package com.myfzone_sport.myf_zone.fragments.event.event_creation

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentEventCreationBinding
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth.isAffiliated
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth.isConnected
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventData
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.onItemSelectedListener
import org.jetbrains.anko.support.v4.toast
import java.text.SimpleDateFormat
import java.util.*


class EventCreationFragment : Fragment() {
    companion object {
        private val TAG = EventCreationFragment::class.java.simpleName

        private var eventDay1: String? = null
        private var eventDay2: String? = null
        private var eventTime: String? = null
        private var event: Event = Event()
        private const val AUTOCOMPLETE_REQUEST_CODE = 1

        private lateinit var viewModel: EventCreationViewModel
        private lateinit var binding: FragmentEventCreationBinding
    }

    //region Override Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_event_creation,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(
            EventCreationViewModel::class.java
        )

        resetFields()

        binding.apply {
            lifecycleOwner = this@EventCreationFragment
            executePendingBindings()
        }

        binding.event =
            event

        //Event Address
        Places.initialize(requireContext(), getString(R.string.google_maps_key))

        binding.eventCreateDayPicker.setOnClickListener {
            selectDate()
        }

        binding.eventCreateDateLayout.setEndIconOnClickListener {
            selectDate()
        }

        binding.eventCreateTimePicker.setOnClickListener {
            selectTime()
        }

        binding.eventCreateTimeLayout.setEndIconOnClickListener {
            selectTime()
        }

        binding.eventCreateTypeInput.setOnClickListener {
            binding.eventCreateTypeSpinner.performClick()
        }

        binding.eventCreateTypeLayout.setEndIconOnClickListener {
            binding.eventCreateTypeSpinner.performClick()
        }

        binding.eventCreateTeamInput.setOnClickListener {
            binding.eventCreateTeamSpinner.performClick()
        }

        binding.eventCreateTeamLayout.setEndIconOnClickListener {
            binding.eventCreateTeamSpinner.performClick()
        }

        setStartDate()

        binding.eventCreateTypeSpinner.setSelection(0)
        binding.eventCreateTypeSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, selected ->
                val longVal: Long = 0
                if (selected == longVal) {
                    val list = resources.getStringArray(R.array.amicalTeamList)
                    val adapter = ArrayAdapter(requireContext(), R.layout.simple_layout_file, list)
                    binding.eventCreateTeamSpinner.isEnabled = false
                    binding.eventCreateTeamSpinner.adapter = adapter

                } else {
                    val list = resources.getStringArray(R.array.teamList)
                    val adapter = ArrayAdapter(requireContext(), R.layout.simple_layout_file, list)
                    binding.eventCreateTeamSpinner.isEnabled = true
                    binding.eventCreateTeamSpinner.adapter = adapter
                }

                binding.eventCreateTypeInput.setText(binding.eventCreateTypeSpinner.selectedItem.toString())
            }
        }

        binding.eventCreateTeamSpinner.setSelection(0)
        binding.eventCreateTeamSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, selected ->
                val text =
                    binding.eventCreateTeamSpinner.selectedItem.toString() + getString(R.string.teams)
                binding.eventCreateTeamInput.setText(text)

                val nbTeam = 2 + selected
                viewModel.setEventTeam(
                    nbTeam.toInt(),
                    event
                )
            }
        }

        binding.eventCreateAddressInput.setOnClickListener {
            setupAddressIntent(viewModel.fields.value!!)
        }

        binding.eventCreateButton.setOnClickListener {
            if (isConnected) {
                if (isAffiliated) {
                    createEvent()
                } else {
                    toast(getString(R.string.new_event_error_msg))
                }
            } else {
                toast(getString(R.string.new_event_error_msg))
            }
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                if (data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    binding.eventCreateAddressInput.setText(place.address)
                    event.address = place.address!!
                    event.lat = place.latLng?.latitude!!
                    event.lng = place.latLng?.longitude!!
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        binding.eventCreateAddressInput.hideKeyboard()
        resetFields()
    }
    //endregion

    //region Event Creation
    private fun createEvent() {
        try {
            if (validateForm())
                confirmCreation()
        } catch (e: Exception) {
            toast("Erreur: ${e.localizedMessage}")
            Log.d(TAG, "Error: $e")
        }
    }

    private fun confirmCreation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.create_event))
            .setMessage(getString(R.string.create_event_confirmation))
            .setIcon(R.drawable.ic_info)
            .setPositiveButton(getString(R.string.create_txt)) { _: DialogInterface, _: Int ->
                viewModel.setEventDate(
                    eventDay1!!, eventDay2!!, eventTime!!,
                    event
                )

                Log.i(TAG, "type? ${binding.eventCreateTypeSpinner.selectedItem}")

                viewModel.setEventType(
                    binding.eventCreateTypeSpinner.selectedItem.toString(),
                    event
                )

                lifecycleScope.launch {
                    getOwnerForEvent()
                }

                Log.i(TAG, "Event is = $event")
            }.setNegativeButton(getString(R.string.cancel_message)) { _: DialogInterface, _: Int ->
            }.show()
    }

    private suspend fun getOwnerForEvent() {
        viewModel.getOwnerForEvent().collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    hideProgressBar()
                    val owner = state.data.first
                    val club = state.data.second
                    createNewEvent()
                    addOwnerToEvent(owner)
                    addEventToUser(owner, club)
                    TRACKING.logEvent(Tracking.EVENT_CREATION_DONE, null)
                    (getString(R.string.event_created)).toast()
                    resetFields()
                    requireActivity().onBackPressed()
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "Erreur : ${state.message}"
                    message.toast()
                }
            }
        }
    }

    private suspend fun createNewEvent() {
        viewModel.createEvent(
            event
        ).collect { state ->
            when (state) {
                is State.Loading -> {

                }
                is State.Success -> {
                    EventData.newEvent = event
                    Log.i(TAG, "EventData.event: ${EventData.newEvent}")
                    Log.i(TAG, "Success! Event is: ${state.data}")
                }
                is State.Failed -> {
                    val message = "An error occurred: ${state.message}"
                    message.toast()
                }
            }
        }
    }

    private suspend fun addOwnerToEvent(owner: EventOwner) {
        viewModel.addOwnerToEvent(
            event, owner
        ).collect { state ->
            when (state) {
                is State.Loading -> {

                }
                is State.Success -> {
                    Log.i(TAG, "Success! Boolean is: ${state.data}")
                }
                is State.Failed -> {
                    val message = "An error occurred: ${state.message}"
                    message.toast()
                }
            }
        }
    }

    private suspend fun addEventToUser(owner: EventOwner, clubAffiliation: ClubAffiliation) {
        viewModel.addEventToUser(
            event, owner, clubAffiliation
        ).collect { state ->
            when (state) {
                is State.Loading -> {

                }
                is State.Success -> {
                    Log.i(TAG, "Success! Event is: ${state.data}")
                }
                is State.Failed -> {
                    val message = "An error occurred: ${state.message}"
                    message.toast()
                }
            }
        }
    }

    private fun setupAddressIntent(fields: MutableList<Place.Field>) {
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).apply {
                setHint(getString(R.string.enter_address_hint))
            }
                .build(requireContext())
        startActivityForResult(
            intent,
            AUTOCOMPLETE_REQUEST_CODE
        )
    }
    //endregion

    //region Event Time
    private fun setStartDate() {
        binding.eventCreateDayPicker.text = getString(R.string.event_creation_date_btn).toEditable()
        binding.eventCreateTimePicker.text =
            getString(R.string.event_creation_time_btn).toEditable()
    }

    private fun selectDate() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dateListener = DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                cal.set(Calendar.DAY_OF_MONTH, d)
                cal.set(Calendar.MONTH, m)
                cal.set(Calendar.YEAR, y)
                binding.eventCreateDayPicker.text =
                    SimpleDateFormat("dd/MM/y", Locale.FRANCE).format(cal.time).toEditable()
                eventDay1 = SimpleDateFormat("E MMM dd", Locale.ENGLISH).format(cal.time)
                eventDay2 = SimpleDateFormat("z yyyy", Locale.ENGLISH).format(cal.time)
            },
            year,
            month,
            day
        )
        dateListener.datePicker.minDate = cal.timeInMillis
        dateListener.show()
    }

    private fun selectTime() {
        val cal = Calendar.getInstance()
        val timeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            binding.eventCreateTimePicker.text =
                SimpleDateFormat("HH:mm", Locale.FRANCE).format(cal.time).toEditable()
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
    //endregion

    //region Form Validation
    private fun validateForm(): Boolean {
        var valid = true

        val title = binding.eventCreateTitleInput.text.toString()
        val description = binding.eventCreateDescInput.text.toString()
        val address = binding.eventCreateAddressInput.text.toString()

        val titleLayout = binding.eventCreateTitleLayout
        val descriptionLayout = binding.eventCreateDescLayout
        val addressLayout = binding.eventCreateAddressLayout

        when {
            TextUtils.isEmpty(title.trim()) -> {
                titleLayout.error = getString(R.string.hint_required)
                valid = false
            }
            title.trim().length > 21 -> {
                titleLayout.error = getString(R.string.hint_required)
                valid = false
            }
            else -> {
                titleLayout.error = null
            }
        }

        when {
            TextUtils.isEmpty(description.trim()) -> {
                descriptionLayout.error = getString(R.string.hint_required)
                valid = false
            }
            description.trim().length > 51 -> {
                descriptionLayout.error = getString(R.string.hint_required)
                valid = false
            }
            else -> {
                descriptionLayout.error = null
            }
        }

        if (TextUtils.isEmpty(address.trim())) {
            addressLayout.error = getString(R.string.hint_required)
            valid = false
        } else {
            addressLayout.error = null
        }

        if (eventDay1 != null && eventDay2 != null && eventTime != null) {
            if (viewModel.setEventDate(
                    eventDay1!!, eventDay2!!, eventTime!!,
                    event
                ) !is Date
            ) {
//                toast(getString(R.string.date_required))
                valid = false
            }
        } else {
            toast(getString(R.string.date_required))
            valid = false
        }

        return valid
    }
    //endregion

    //region Loading
    private fun showProgressBar() {
        binding.progressBar.apply {
            visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        binding.progressBar.apply {
            visibility = View.GONE
        }
    }
    //endregion

    //region View Methods
    private fun resetFields() {
        binding.eventCreateTitleInput.setText("")
        binding.eventCreateDescInput.setText("")
        binding.eventCreateTypeSpinner.setSelection(0)
        binding.eventCreateTeamSpinner.setSelection(0)
//        setStartDate()
        binding.eventCreateAddressInput.setText("")
    }

    private fun String.toast() {
        toast(this)
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
    //endregion
}