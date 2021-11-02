package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.chat.MessagingService
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.usecases.detailevent.AcceptParticipantUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetEventFromIdUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.RefuseParticipantUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 01/11/2021, 12:40
 */

class EventParticipantsOwnerViewModel(
    private val getEventFromIdUseCase: GetEventFromIdUseCase,
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
    private val acceptParticipantUseCase: AcceptParticipantUseCase,
    private val refuseParticipantUseCase: RefuseParticipantUseCase
) : ViewModel() {

    //region Variables
    private val _event = MutableLiveData<Event>()
    val event = _event

    private val eventParticipants = MutableLiveData<MutableList<EventParticipant>>()

    private val _eventParticipantsValid = MutableLiveData<MutableList<EventParticipant>>()
    val eventParticipantsValid = _eventParticipantsValid

    private val _eventParticipantsPending = MutableLiveData<MutableList<EventParticipant>>()
    val eventParticipantsPending = _eventParticipantsPending

    private val _eventParticipantsRefused = MutableLiveData<MutableList<EventParticipant>>()
    val eventParticipantsRefused = _eventParticipantsRefused

    private val firebaseFirestore = Firebase.firestore

    private val _userImagePath = MutableLiveData<StorageReference>()
    val userImagePath: LiveData<StorageReference> = _userImagePath

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    fun assignEventId(eventId: String) {
        getParticipants(eventId)
        getEvent(eventId)
    }

    private fun getEvent(eventId: String) {
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

    private fun getParticipants(eventId: String) {
        viewModelScope.launch {
            getAllParticipantsFromEventUseCase.invoke(eventId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val participantList = state.data
                        eventParticipants.postValue(participantList)

                        assignValidParticipants(participantList)
                        assignPendingParticipants(participantList)
                        assignRefusedParticipants(participantList)
                        onResult()
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
            if (participant.status == "validate") result.add(participant)

        _eventParticipantsValid.postValue(result)
    }

    private fun assignPendingParticipants(list: MutableList<EventParticipant>) {
        val result = mutableListOf<EventParticipant>()
        for (participant in list)
            if (participant.status == "pending") result.add(participant)

        _eventParticipantsPending.postValue(result)
    }

    private fun assignRefusedParticipants(list: MutableList<EventParticipant>) {
        val result = mutableListOf<EventParticipant>()
        for (participant in list)
            if (participant.status == "refused") result.add(participant)

        _eventParticipantsRefused.postValue(result)
    }

    fun acceptParticipant(eventId: String, participant: EventParticipant) {
        viewModelScope.launch {
            acceptParticipantUseCase.invoke(eventId, participant).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
//                        notifyParticipantAccepted(_event.value!!, state.data)
                        getParticipants(eventId)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Accepting participant failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun refuseParticipant(eventId: String, participant: EventParticipant) {
        viewModelScope.launch {
            refuseParticipantUseCase.invoke(eventId, participant).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
//                        notifyParticipantRefused(_event.value!!, state.data)
                        getParticipants(eventId)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Refusing participant failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun notifyParticipantAccepted(event: Event, participant: EventParticipant) {
        MessagingService.eventAcceptParticipation(event, participant)
    }

    private fun notifyParticipantRefused(event: Event, participant: EventParticipant) {
        MessagingService.eventRefuseParticipation(event, participant)
    }

    fun getQuery(eventId: String): CollectionReference {
        return firebaseFirestore
            .collection(Constants.EVENT_PATH)
            .document(eventId)
            .collection("Participant")
    }

    fun getImageReference(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _userImagePath.postValue(getImageReferenceUseCase.invoke(path))
        }
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

class EventParticipantsOwnerViewModelFactory(
    private val getEventFromIdUseCase: GetEventFromIdUseCase,
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
    private val acceptParticipantUseCase: AcceptParticipantUseCase,
    private val refuseParticipantUseCase: RefuseParticipantUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetEventFromIdUseCase::class.java,
            GetAllParticipantsFromEventUseCase::class.java,
            GetImageReferenceUseCase::class.java,
            AcceptParticipantUseCase::class.java,
            RefuseParticipantUseCase::class.java
        )
            .newInstance(
                getEventFromIdUseCase,
                getAllParticipantsFromEventUseCase,
                getImageReferenceUseCase,
                acceptParticipantUseCase,
                refuseParticipantUseCase
            )
    }
}