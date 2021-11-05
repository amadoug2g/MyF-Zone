package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.*
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetEventFromIdUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetOwnerFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 14/10/2021, 18:17
 */

class EventDetailsOwnerViewModel(
    private val getEventFromIdUseCase: GetEventFromIdUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
    private val getOwnerFromEventUseCase: GetOwnerFromEventUseCase,
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase,
) : ViewModel() {

    //region Variables
    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    private val _eventOwner = MutableLiveData<EventOwner>()
    val eventOwner: LiveData<EventOwner> = _eventOwner

    private val _eventOwnerToken = MutableLiveData<MutableList<String>>()
    val eventOwnerToken: LiveData<MutableList<String>> = _eventOwnerToken

    private val _userImagePath = MutableLiveData<StorageReference>()
    val userImagePath: LiveData<StorageReference> = _userImagePath

    private val _eventParticipants = MutableLiveData<MutableList<EventParticipant>>()

    private val _eventParticipantsValid = MutableLiveData<MutableList<EventParticipant>>()
    val eventParticipantsValid: LiveData<MutableList<EventParticipant>> = _eventParticipantsValid

    private val eventId = MutableLiveData<String>()
    val validParticipantCount = MutableLiveData(0)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    fun assignEventId(eventId: String) {
        this.eventId.postValue(eventId)
        getEvent(eventId)
        getOwner(eventId)
        getParticipants(eventId)
    }

    private fun getEvent(eventId: String = this.eventId.value!!) {
        viewModelScope.launch {
            getEventFromIdUseCase.invoke(eventId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val event = state.data

                        _event.postValue(event)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Event fetching failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getImageReference(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _userImagePath.postValue(getImageReferenceUseCase.invoke(path))
        }
    }

    private fun getOwner(eventId: String = this.eventId.value!!) {
        viewModelScope.launch {
            getOwnerFromEventUseCase.invoke(eventId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val owner = state.data

                        _eventOwner.postValue(owner)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Event owner fetching failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getParticipants(eventId: String = this.eventId.value!!) {
        viewModelScope.launch {
            getAllParticipantsFromEventUseCase.invoke(eventId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        val participantList = state.data

                        _eventParticipants.postValue(participantList)
                        assignValidParticipants(participantList)
                    }
                    is State.Failed -> {
                        val message = "Event participants fetching failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun assignValidParticipants(list: MutableList<EventParticipant>) {
        val result = mutableListOf<EventParticipant>()

        for (participant in list)
            if (participant.status == "validate") {
                result.add(participant)
            }

        _eventParticipantsValid.postValue(result)
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
    //endregion
}

class EventDetailsOwnerViewModelFactory(
    private val getEventFromIdUseCase: GetEventFromIdUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
    private val getOwnerFromEventUseCase: GetOwnerFromEventUseCase,
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase,
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetEventFromIdUseCase::class.java,
            GetImageReferenceUseCase::class.java,
            GetOwnerFromEventUseCase::class.java,
            GetAllParticipantsFromEventUseCase::class.java,
        )
            .newInstance(
                getEventFromIdUseCase,
                getImageReferenceUseCase,
                getOwnerFromEventUseCase,
                getAllParticipantsFromEventUseCase,
            )
    }
}