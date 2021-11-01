package com.myfzone_sport.myf_zone.app.ui.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.NewEventViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.NewEventViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentNewEventBinding
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.usecases.newevent.AddNewEventToUserUseCase
import com.myfzone_sport.myf_zone.usecases.newevent.AddOwnerToEventUseCase
import com.myfzone_sport.myf_zone.usecases.newevent.CreateEventUseCase
import com.myfzone_sport.myf_zone.usecases.newevent.GetOwnerForNewEventUseCase
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Tracking
import com.myfzone_sport.myf_zone.util.Tracking.ALERT_ERROR
import org.jetbrains.anko.sdk27.coroutines.onItemSelectedListener
import org.jetbrains.anko.support.v4.toast
import java.text.SimpleDateFormat
import java.util.*

class NewEventFragment : Fragment() {

    //region Variables
    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    binding.eventCreateAddressInput.setText(place.address)
                    event.address = place.address!!
                    event.lat = place.latLng?.latitude!!
                    event.lng = place.latLng?.longitude!!
                }
            }
        }

    companion object {
        private lateinit var binding: FragmentNewEventBinding
        private lateinit var viewModel: NewEventViewModel
        private lateinit var viewModelFactory: NewEventViewModelFactory

        private var eventDay1: String? = null
        private var eventDay2: String? = null
        private var eventTime: String? = null
        private var event: Event = Event()
        private const val AUTOCOMPLETE_REQUEST_CODE = 1
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_event, container, false)

        setupViews()
        setupObservers()

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                if (data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    binding.eventCreateAddressInput.setText(place.address)
//                    viewModel.setEventAddress(place)
//                    viewModel.assignAddress(place)
                    event.address = place.address!!
                    event.lat = place.latLng?.latitude!!
                    event.lng = place.latLng?.longitude!!

                    binding.progressBar.hideKeyboard()
                }
            }
        }
    }
    //endregion

    //region Setups
    private fun setupViewModel() {
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getOwnerForNewEventUseCase = GetOwnerForNewEventUseCase(repository)
        val addOwnerToEventUseCase = AddOwnerToEventUseCase(repository)
        val addNewEventToUserUseCase = AddNewEventToUserUseCase(repository)
        val createEventUseCase = CreateEventUseCase(repository)

        viewModelFactory = NewEventViewModelFactory(
            getOwnerForNewEventUseCase,
            addOwnerToEventUseCase,
            addNewEventToUserUseCase,
            createEventUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(NewEventViewModel::class.java)
    }

    private fun setupViews() {

        binding.viewModel = viewModel

        setStartDate()
        setupFields()

        binding.apply {
            lifecycleOwner = this@NewEventFragment
            executePendingBindings()
        }

        binding.exitNewEvent.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.eventCreateDayPicker.setOnClickListener {
            selectDate()
        }

        binding.eventCreateTimePicker.setOnClickListener {
            selectTime()
        }

        binding.eventCreateAddressInput.setOnClickListener {
            setupAddressIntent(viewModel.fields.value!!)
        }

        binding.eventCreateButton.setOnClickListener {
            confirmCreation()
        }
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })

        viewModel.errorMessageTitle.observe(viewLifecycleOwner, {
            if (it) {
                val bundleTracking = bundleOf("New Event Title Error" to getString(R.string.hint_required))
                TRACKING.logEvent(ALERT_ERROR, bundleTracking)
                binding.titleLayout.error = getString(R.string.hint_required)
            } else {
                binding.titleLayout.error = null
            }
        })

        viewModel.errorMessageDesc.observe(viewLifecycleOwner, {
            if (it) {
                val bundleTracking = bundleOf("New Event Description Error" to getString(R.string.hint_required))
                TRACKING.logEvent(ALERT_ERROR, bundleTracking)
                binding.descriptionLayout.error = getString(R.string.hint_required)
            } else {
                binding.descriptionLayout.error = null
            }
        })

        viewModel.successfulEventCreation.observe(viewLifecycleOwner, { state ->
            if (state) {
                Constants.TRACKING.logEvent(Tracking.EVENT_CREATION_DONE, null)
                (getString(R.string.event_created)).toast()
                requireActivity().onBackPressed()
            }
        })
    }

    private fun setupFields() {
        Places.initialize(requireContext(), getString(R.string.google_maps_key))

        binding.typeSpinner.setSelection(0)
        binding.teamNumberSpinner.setSelection(0)

        binding.typeSpinner.onItemSelectedListener {
            onItemSelected { _, _, _, selected ->
                val longVal: Long = 0
                if (selected == longVal) {
                    val list = resources.getStringArray(R.array.amicalTeamList)
                    val adapter = ArrayAdapter(requireContext(), R.layout.simple_layout_file, list)
                    binding.teamNumberSpinner.isEnabled = false
                    binding.teamNumberSpinner.adapter = adapter
                } else {
                    val list = resources.getStringArray(R.array.teamList)
                    val adapter = ArrayAdapter(requireContext(), R.layout.simple_layout_file, list)
                    binding.teamNumberSpinner.isEnabled = true
                    binding.teamNumberSpinner.adapter = adapter
                }
            }
        }
    }

    private fun setupAddressIntent(fields: MutableList<Place.Field>) {
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setHint(getString(R.string.enter_address_hint))
            .build(requireContext())

        activityLauncher.launch(intent)
    }
    //endregion

    //region Event
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

    private fun confirmCreation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.create_event))
            .setMessage(getString(R.string.create_event_confirmation))
            .setIcon(R.drawable.ic_info)
            .setPositiveButton(getString(R.string.create_txt)) { _: DialogInterface, _: Int ->
                createEvent()
            }.setNegativeButton(getString(R.string.cancel_message)) { _: DialogInterface, _: Int ->
            }.show()
    }

    private fun createEvent() {
        viewModel.assignEventText(event)
        viewModel.setEventDate(eventDay1!!, eventDay2!!, eventTime!!, event)
        viewModel.setEventType(binding.typeSpinner.selectedItem.toString(), event)
        viewModel.setEventTeam(binding.teamNumberSpinner.selectedItem.toString().toInt(), event)
        viewModel.newEvent(event)
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null, view: View) {
        Navigation
            .findNavController(view)
            .navigate(destination, extra)
    }
    //endregion

    //region View Methods
    private fun setStartDate() {
        binding.eventCreateDayPicker.text = getString(R.string.event_creation_date_btn).toEditable()
        binding.eventCreateTimePicker.text =
            getString(R.string.event_creation_time_btn).toEditable()
    }

    private fun String.toast() {
        toast(this)
    }

    private fun loadingStart() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun loadingStop() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showError(message: String? = "") {

        Snackbar.make(
            binding.progressBar,
            if (!message.isNullOrEmpty()) message else viewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    //endregion
}