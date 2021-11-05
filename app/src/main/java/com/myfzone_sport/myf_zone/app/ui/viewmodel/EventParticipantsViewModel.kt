package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetEventFromIdUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 16/10/2021, 19:48
 */

class EventParticipantsViewModel(
    private val getEventFromIdUseCase: GetEventFromIdUseCase,
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase
) : ViewModel() {

    //region Variables
    private val _event = MutableLiveData<Event>()
    val event = _event

    private val eventParticipants = MutableLiveData<MutableList<EventParticipant>>()

    private val _eventParticipantsValid = MutableLiveData<MutableList<EventParticipant>>()
    val eventParticipantsValid = _eventParticipantsValid

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

    fun getQuery(eventId: String): CollectionReference {
        return firebaseFirestore
            .collection(EVENT_PATH)
            .document(eventId)
            .collection("Participant")
    }

    fun getImageReference(path: String): StorageReference {
        return getImageReferenceUseCase.invoke(path)
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

class EventParticipantsViewModelFactory(
    private val getEventFromIdUseCase: GetEventFromIdUseCase,
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetEventFromIdUseCase::class.java,
            GetAllParticipantsFromEventUseCase::class.java,
            GetImageReferenceUseCase::class.java,
        )
            .newInstance(
                getEventFromIdUseCase,
                getAllParticipantsFromEventUseCase,
                getImageReferenceUseCase,
            )
    }
}