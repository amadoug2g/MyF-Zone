    package mfz.myfzone_sport.myf_zone.fragments.event.event_edit

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentEventEditBinding
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.event.Event
import org.jetbrains.anko.sdk27.coroutines.onItemSelectedListener
import org.jetbrains.anko.support.v4.toast
import java.text.SimpleDateFormat
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "eventId"

class EventEditFragment : Fragment() {
    companion object {
        private val TAG = EventEditFragment::class.java.simpleName
        private var eventId: String? = null

        private var eventDay1: String? = null
        private var eventDay2: String? = null
        private var eventTime: String? = null
        private const val AUTOCOMPLETE_REQUEST_CODE = 1

        private lateinit var viewModel: EventEditViewModel
        private lateinit var viewModelFactory: EventEditViewModelFactory
        private lateinit var binding: FragmentEventEditBinding
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_event_edit,
            container,
            false
        )

        viewModelFactory = EventEditViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory).get(EventEditViewModel::class.java)

        binding.apply {
            lifecycleOwner = this@EventEditFragment
            executePendingBindings()
        }

        lifecycleScope.launch {
            getEvent()
        }

        //Event Address
        Places.initialize(requireContext(), getString(R.string.google_maps_key))

        binding.eventEditDayPicker.setOnClickListener { selectDate() }
        binding.eventEditTimePicker.setOnClickListener { selectTime() }

        binding.eventEditTypeSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, selected ->
                val longVal: Long = 0
                if (selected == longVal) {
                    val list = resources.getStringArray(R.array.amicalTeamList)
                    val adapter = ArrayAdapter(requireContext(), R.layout.simple_layout_file, list)
                    binding.eventEditTeamSpinner.isEnabled = false
                    binding.eventEditTeamSpinner.adapter = adapter
                } else {
                    val list = resources.getStringArray(R.array.teamList)
                    val adapter = ArrayAdapter(requireContext(), R.layout.simple_layout_file, list)
                    binding.eventEditTeamSpinner.isEnabled = true
                    binding.eventEditTeamSpinner.adapter = adapter
                }
                viewModel.setEventType(binding.eventEditTypeSpinner.selectedItem.toString())
            }
        }

        binding.eventEditTeamSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, selected ->
                val nbTeam = 2 + selected
                viewModel.setEventTeam(nbTeam.toInt())
            }
        }

        binding.eventEditAddressInput.setOnClickListener { setupAddressIntent(viewModel.fields.value!!) }
        binding.eventEditButton.setOnClickListener {

            try {
                if (validateForm()) {
                    confirmUpdate()
                }
            } catch (e: Exception) {
                toast("Error on Edit: ${e.localizedMessage}")
                Log.d(TAG, "Error: $e")
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
                    binding.eventEditAddressInput.setText(place.address)
                    viewModel.event.value?.address = place.address!!
                    viewModel.event.value?.lat = place.latLng?.latitude!!
                    viewModel.event.value?.lng = place.latLng?.longitude!!
                }
            }
        }
    }
    //endregion

    private suspend fun getEvent() {
        viewModel.getEvent(eventId!!).collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    hideProgressBar()
                    val event = state.data
                    viewModel.event.value = event
                    linkPendingBindings(event)
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    message.toast()
                }
            }
        }
    }

    private suspend fun updateEvent() {
        viewModel.updateEvent(viewModel.event.value!!.id, viewModel.event.value!!)
            .collect { state ->
                when (state) {
                    is State.Loading -> {
                        showProgressBar()
                    }
                    is State.Success -> {
                        hideProgressBar()
                        val message = ("Event successfully updated")
                        message.toast()
                        requireActivity().onBackPressed()
                    }
                    is State.Failed -> {
                        hideProgressBar()
                        val message = "An error occurred: ${state.message}"
                        message.toast()
                    }
                }
            }
    }

    private fun linkPendingBindings(event: Event) {
        binding.event = event

        try {
            binding.eventEditTypeSpinner.setSelection(viewModel.getEventTypeToDisplay(event))
        } catch (e: Exception) {
            val message = "Error in selecting the type of event: $e"
            message.toast()
        }

        try {
            binding.eventEditTeamSpinner.setSelection(viewModel.getEventNbTeamToDisplay(event))
        } catch (e: Exception) {
            val message = "Error in selecting the number of participant: $e"
            message.toast()
        }

        binding.eventDate = viewModel.getEventDay(event)
        binding.eventTime = viewModel.getEventHour(event)

        setEventDate(event)
    }

    private fun setupAddressIntent(fields: MutableList<Place.Field>) {
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).apply {
                setHint(getString(R.string.enter_address_hint))
            }
                .build(requireContext())
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    //region Event Time
    private fun setEventDate(event: Event) {
        val formatEventDay1 = SimpleDateFormat("E MMM dd", Locale.ENGLISH)
        val formatEventDay2 = SimpleDateFormat("z yyyy", Locale.ENGLISH)
        val formatEventHour = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

        eventDay1 = formatEventDay1.format(formatDate.parse(event.date.toString())!!)
        eventDay2 = formatEventDay2.format(formatDate.parse(event.date.toString())!!)
        eventTime = formatEventHour.format(formatDate.parse(event.date.toString())!!)
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
                binding.eventEditDayPicker.text =
                    SimpleDateFormat("dd/MM/y", Locale.FRANCE).format(cal.time)
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
            binding.eventEditTimePicker.text =
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
    //endregion

    //region Form Validation
    private fun validateForm(): Boolean {
        var valid = true

        val title = binding.event!!.title
        val description = binding.event!!.description
        val address = binding.event!!.address

        val titleLayout = binding.eventEditTitleLayout
        val descriptionLayout = binding.eventEditDescLayout
        val addressLayout = binding.eventEditAddressLayout

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

        if (viewModel.setEventDate(eventDay1!!, eventDay2!!, eventTime!!) !is Date) {
            (getString(R.string.date_required)).toast()
            valid = false
        }

        return valid
    }
    //endregion

    private fun confirmUpdate() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.edit_event))
            .setMessage(getString(R.string.edit_event_confirmation))
            .setIcon(R.drawable.ic_info)
            .setPositiveButton(getString(R.string.edit_txt)) { _: DialogInterface, _: Int ->
                viewModel.setEventDate(eventDay1!!, eventDay2!!, eventTime!!)

                lifecycleScope.launch {
                    updateEvent()
                }

                Log.i(TAG, "Event is = ${viewModel.event.value}")
            }.setNegativeButton(getString(R.string.cancel_message)) { _: DialogInterface, _: Int ->
            }.show()
    }

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
    private fun String.toast() {
        toast(this)
    }
    //endregion
}