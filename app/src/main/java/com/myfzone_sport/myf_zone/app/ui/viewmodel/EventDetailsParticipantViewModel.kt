package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.*
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.chat.MessagingService
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.usecases.detailevent.*
import com.myfzone_sport.myf_zone.usecases.notification.GetOwnerTokenUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.util.Tracking
import com.myfzone_sport.myf_zone.util.Tracking.ALERT_ERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 14/10/2021, 18:24
 */

class EventDetailsParticipantViewModel(
    private val getEventFromIdUseCase: GetEventFromIdUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
    private val getOwnerFromEventUseCase: GetOwnerFromEventUseCase,
    private val getOwnerTokenUseCase: GetOwnerTokenUseCase,
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase,
    private val joinEventUseCase: JoinEventUseCase,
    private val leaveEventUseCase: LeaveEventUseCase
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

    private val _eventParticipantsValid = MutableLiveData<MutableList<EventParticipant>>()
    val eventParticipantsValid: LiveData<MutableList<EventParticipant>> = _eventParticipantsValid

    private val eventId = MutableLiveData<String>()

    private val _isUserParticipating = MutableLiveData<Boolean>()
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

    private fun getEvent(eventId: String) {
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

    private fun getOwner(eventId: String) {
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

    fun getOwnerToken(ownerId: String) {
        viewModelScope.launch {
            getOwnerTokenUseCase.invoke(ownerId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        val tokenList = state.data

                        _eventOwnerToken.postValue(tokenList)
                    }
                    is State.Failed -> {
                        val message = "Event owner token fetching failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getParticipants(eventId: String) {
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
                        isCoachParticipant(participantList)
                    }
                    is State.Failed -> {
                        val message = "Event participants fetching failure: ${state.message}"
                        onResult(message)

                        val bundleTracking =
                            bundleOf("Event participants fetching failure" to state.message)
                        TRACKING.logEvent(ALERT_ERROR, bundleTracking)
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

    fun joinEvent(eventId: String, participant: EventParticipant) {
        viewModelScope.launch {
            joinEventUseCase.invoke(eventId, participant).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        _isUserParticipating.postValue(true)
//                        MessagingService.eventParticipation(_event.value!!, _eventOwner.value!!)
//                        TRACKING.logEvent(Tracking.EVENT_DETAILS_OWNER_ACCEPT_PARTICIPATION, null)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Failed to join event: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun leaveEvent(eventId: String) {
        viewModelScope.launch {
            leaveEventUseCase.invoke(eventId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        _isUserParticipating.postValue(false)
//                        TRACKING.logEvent(Tracking.EVENT_DETAILS_OWNER_ACCEPT_PARTICIPATION, null)
                        getParticipants(eventId)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Failed to leave event: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun isCoachParticipant(list: MutableList<EventParticipant>) {
        var result = false

        for (participant in list)
            if (participant.coachId == activeCoach?.id) result = true

        _isUserParticipating.postValue(result)
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

class EventDetailsParticipantViewModelFactory(
    private val getEventFromIdUseCase: GetEventFromIdUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
    private val getOwnerFromEventUseCase: GetOwnerFromEventUseCase,
    private val getOwnerTokenUseCase: GetOwnerTokenUseCase,
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase,
    private val joinEventUseCase: JoinEventUseCase,
    private val leaveEventUseCase: LeaveEventUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetEventFromIdUseCase::class.java,
            GetImageReferenceUseCase::class.java,
            GetOwnerFromEventUseCase::class.java,
            GetOwnerTokenUseCase::class.java,
            GetAllParticipantsFromEventUseCase::class.java,
            JoinEventUseCase::class.java,
            LeaveEventUseCase::class.java
        )
            .newInstance(
                getEventFromIdUseCase,
                getImageReferenceUseCase,
                getOwnerFromEventUseCase,
                getOwnerTokenUseCase,
                getAllParticipantsFromEventUseCase,
                joinEventUseCase,
                leaveEventUseCase
            )
    }
}