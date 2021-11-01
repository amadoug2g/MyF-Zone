package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.*
import com.google.android.libraries.places.api.model.Place
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.usecases.newevent.AddNewEventToUserUseCase
import com.myfzone_sport.myf_zone.usecases.newevent.AddOwnerToEventUseCase
import com.myfzone_sport.myf_zone.usecases.newevent.CreateEventUseCase
import com.myfzone_sport.myf_zone.usecases.newevent.GetOwnerForNewEventUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Amadou on 19/10/2021, 21:47
 */

class NewEventViewModel(
    private val getOwnerForNewEventUseCase: GetOwnerForNewEventUseCase,
    private val addOwnerToEventUseCase: AddOwnerToEventUseCase,
    private val addNewEventToUserUseCase: AddNewEventToUserUseCase,
    private val createEventUseCase: CreateEventUseCase
) : ViewModel() {

    //region Variables
    val newEventTitle = MutableLiveData<String>()
    val newEventDesc = MutableLiveData<String>()

    val event = MutableLiveData<Event>()

    private val _eventOwner = MutableLiveData<EventOwner>()
    val eventOwner: LiveData<EventOwner> = _eventOwner

    private val _fields = MutableLiveData<MutableList<Place.Field>>()
    val fields: LiveData<MutableList<Place.Field>> = _fields

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _errorMessageTitle = MutableLiveData(false)
    val errorMessageTitle: LiveData<Boolean> = _errorMessageTitle

    private val _errorMessageDesc = MutableLiveData(false)
    val errorMessageDesc: LiveData<Boolean> = _errorMessageDesc

    private val _successfulEventCreation = MutableLiveData(false)
    val successfulEventCreation = _successfulEventCreation
    //endregion

    //region Functions
    init {
        initFields()
    }

    private fun initFields() {
        _fields.value = mutableListOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
    }

    fun newEvent(event: Event) {
        if (validateNewEventFields()) {
            getOwner(event)
        } else {
            onResult("Enter a title and description!")
        }
    }

    private fun getOwner(event: Event) {
        viewModelScope.launch {
            getOwnerForNewEventUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val owner = state.data.first
                        val club = state.data.second

                        createEvent(event)
                        addOwnerToEvent(event, owner)
                        addEventToUser(event, owner, club)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Getting owner failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun addOwnerToEvent(event: Event, owner: EventOwner) {
        viewModelScope.launch {
            addOwnerToEventUseCase.invoke(event, owner).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Adding owner failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun addEventToUser(event: Event, owner: EventOwner, club: ClubAffiliation) {
        viewModelScope.launch {
            addNewEventToUserUseCase.invoke(event, owner, club).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Adding event failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun createEvent(event: Event) {
        viewModelScope.launch {
            createEventUseCase.invoke(event).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        eventCreationComplete()
                    }
                    is State.Failed -> {
                        val message = "Creating event failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun assignEventText(event: Event) {
        Log.i("TAG", "event assignEventText")
        event.title = newEventTitle.value!!
        event.description = newEventDesc.value!!
    }

    private fun validateNewEventFields(): Boolean {
        _errorMessageTitle.value = TextUtils.isEmpty(newEventTitle.value)
        _errorMessageDesc.value = TextUtils.isEmpty(newEventDesc.value)

        return (!_errorMessageTitle.value!! && !_errorMessageDesc.value!!)
    }

    private fun validateNewEventAddress(): Boolean {
        return (event.value?.address != null && event.value?.lat != null && event.value?.lng != null)
    }

    fun setEventType(type: String, event: Event) {
        Log.i("TAG", "event setEventType")
        event.type = when (type) {
            "Match Amical" -> {
                "friendly"
            }
            "Plateau" -> {
                "plateau"
            }
            else -> {
                "tournament"
            }
        }
    }

    fun setEventTeam(nbTeam: Int, event: Event) {
        Log.i("TAG", "event setEventTeam")
        event.nbTeam = nbTeam
    }

    fun setEventDate(eventDay1: String, eventDay2: String, eventTime: String, event: Event): Date? {
        Log.i("TAG", "event setEventDate")
        val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

        val date = formatDate.parse("$eventDay1 $eventTime $eventDay2")

        event.createdDate = Calendar.getInstance().time

        try {
            event.date = date!!
        } catch (e: Exception) {
            Log.d("NewEventVM", "An error occurred in setEventDate: ${e.localizedMessage}")
        }

        return date
    }
    //endregion

    //region Observers
    private fun onResult(message: String = "") {
        _errorMessage.postValue(message)
        stopLoading()
        resetErrorMsg()
    }

    private fun resetErrorMsg() {
        _errorMessage.postValue("")
    }

    private fun startLoading() {
        _isLoading.postValue(true)
    }

    private fun stopLoading() {
        _isLoading.postValue(false)
    }

    private fun eventCreationComplete() {
        _successfulEventCreation.postValue(true)
    }
    //endregion
}

class NewEventViewModelFactory(
    private val getOwnerForNewEventUseCase: GetOwnerForNewEventUseCase,
    private val addOwnerToEventUseCase: AddOwnerToEventUseCase,
    private val addNewEventToUserUseCase: AddNewEventToUserUseCase,
    private val createEventUseCase: CreateEventUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetOwnerForNewEventUseCase::class.java,
            AddOwnerToEventUseCase::class.java,
            AddNewEventToUserUseCase::class.java,
            CreateEventUseCase::class.java,
        )
            .newInstance(
                getOwnerForNewEventUseCase,
                addOwnerToEventUseCase,
                addNewEventToUserUseCase,
                createEventUseCase
            )
    }
}