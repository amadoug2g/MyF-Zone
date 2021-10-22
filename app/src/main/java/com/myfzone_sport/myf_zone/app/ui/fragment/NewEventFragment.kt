package com.myfzone_sport.myf_zone.app.ui.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.libraries.places.api.Places
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
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
import java.text.SimpleDateFormat
import java.util.*

class NewEventFragment : Fragment() {

    //region Variables
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
//        setStartDate()
        Places.initialize(requireContext(), getString(R.string.google_maps_key))

        binding.apply {
            lifecycleOwner = this@NewEventFragment
            executePendingBindings()
        }

        binding.exitNewEvent.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })
    }

    private fun setupEventClicks() {
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

//        binding.eventCreateTypeInput.setOnClickListener {
//            binding.eventCreateTypeSpinner.performClick()
//        }
//
//        binding.eventCreateTypeLayout.setEndIconOnClickListener {
//            binding.eventCreateTypeSpinner.performClick()
//        }
//
//        binding.eventCreateTeamInput.setOnClickListener {
//            binding.eventCreateTeamSpinner.performClick()
//        }
//
//        binding.eventCreateTeamLayout.setEndIconOnClickListener {
//            binding.eventCreateTeamSpinner.performClick()
//        }
    }

    private fun setupFields() {

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
    //endregion
}