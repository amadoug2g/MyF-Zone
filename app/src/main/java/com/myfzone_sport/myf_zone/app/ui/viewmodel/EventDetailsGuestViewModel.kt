package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.*
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.app.framework.FirebaseService
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.usecases.detailevent.*
import com.myfzone_sport.myf_zone.usecases.notification.GetOwnerTokenUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 18/10/2021, 23:38
 */

class EventDetailsGuestViewModel(
    private val getEventFromIdUseCase: GetEventFromIdUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
    private val getOwnerFromEventUseCase: GetOwnerFromEventUseCase,
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase,
) : ViewModel() {

    //region Variables
    private val _event = MutableLiveData<Event>()
    val event = _event

    private val _eventOwner = MutableLiveData<EventOwner>()
    val eventOwner = _eventOwner

    private val _eventOwnerToken = MutableLiveData<MutableList<String>>()
    val eventOwnerToken: LiveData<MutableList<String>> = _eventOwnerToken

    private val _userImagePath = MutableLiveData<StorageReference>()
    val userImagePath: LiveData<StorageReference> = _userImagePath

    private val _eventParticipants = MutableLiveData<MutableList<EventParticipant>>()
    val eventParticipants = _eventParticipants

    private val eventId = MutableLiveData<String>()
    private val coachEventStatus = MutableLiveData<String>()
    val validParticipantCount = MutableLiveData(0)
    private val validParticipantList = MutableLiveData<MutableList<EventParticipant>>()

    private val _isUserParticipating = MutableLiveData(false)
    val isUserParticipating: LiveData<Boolean> = _isUserParticipating

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

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
                        onResult()
                        _event.postValue(state.data)
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
                        onResult()
                        _eventOwner.postValue(state.data)
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
                        _eventParticipants.postValue(state.data)
                        getValidCount(state.data)
                        getValidList(state.data)
                    }
                    is State.Failed -> {
                        val message = "Event participants fetching failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getValidCount(list: MutableList<EventParticipant>) {
        for (participant in list)
            if (participant.status == "valid") validParticipantCount.value?.plus(1)
    }

    private fun getValidList(list: MutableList<EventParticipant>) {
        for (participant in list)
            if (participant.status == "valid") validParticipantList.value?.add(participant)
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

class EventDetailsGuestViewModelFactory(
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
                getAllParticipantsFromEventUseCase
            )
    }
}