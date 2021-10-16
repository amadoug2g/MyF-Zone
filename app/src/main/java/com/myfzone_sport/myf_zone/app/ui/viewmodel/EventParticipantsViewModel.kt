package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 16/10/2021, 19:48
 */

class EventParticipantsViewModel(
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase
) : ViewModel() {

    //region Variables
    private val _eventParticipants = MutableLiveData<MutableList<EventParticipant>>()
    val eventParticipants = _eventParticipants

    private val _eventParticipantsValid = MutableLiveData<MutableList<EventParticipant>>()
    val eventParticipantsValid = _eventParticipantsValid

    private val _eventParticipantsPending = MutableLiveData<MutableList<EventParticipant>>()
    val eventParticipantsPending = _eventParticipantsPending

    private val _eventParticipantsRefused = MutableLiveData<MutableList<EventParticipant>>()
    val eventParticipantsRefused = _eventParticipantsRefused

    private val eventId = MutableLiveData<String>()
    val coachRole = MutableLiveData<String>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    fun assignEventId(eventId: String) {
        this.eventId.postValue(eventId)
        getParticipants(eventId)
    }

    fun assignCoachRole(role: String) {
        this.coachRole.postValue(role)
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

                        assignValidParticipants(state.data)
                        assignPendingParticipants(state.data)
                        assignRefusedParticipants(state.data)
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
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetAllParticipantsFromEventUseCase::class.java
        )
            .newInstance(
                getAllParticipantsFromEventUseCase
            )
    }
}