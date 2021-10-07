package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.usecases.detailevent.GetEventFromIdUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetOwnerFromEventUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 05/10/2021, 12:25
 */

class EventViewModel(
    private val getEventFromIdUseCase: GetEventFromIdUseCase,
    private val getOwnerFromEventUseCase: GetOwnerFromEventUseCase
) : ViewModel() {

    //region Variables
    private val _currentEvent = MutableLiveData<Event>()
    val currentEvent = _currentEvent

    private val _currentEventOwner = MutableLiveData<EventOwner>()
    val currentEventOwner = _currentEventOwner

    val currentEventId = MutableLiveData<String>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    fun getEvent(eventId: String = currentEventId.value!!) {
        viewModelScope.launch(IO) {
            getEventFromIdUseCase.invoke(eventId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        _currentEvent.postValue(state.data)
                    }
                    is State.Failed -> {
                        val message = "Event fetching failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getOwner(eventId: String = currentEventId.value!!) {
        viewModelScope.launch(IO) {
            getOwnerFromEventUseCase.invoke(eventId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        _currentEventOwner.postValue(state.data)
                    }
                    is State.Failed -> {
                        val message = "Event owner fetching failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun assignEventId(eventId: String?) {
        if (eventId != null) {
            currentEventId.postValue(eventId)

//            getEvent()
//            getOwner()
        }
        else
            onResult("Event ID null")
    }

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

class EventViewModelFactory(
    private val getEventFromIdUseCase: GetEventFromIdUseCase,
    private val getOwnerFromEventUseCase: GetOwnerFromEventUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetEventFromIdUseCase::class.java,
            GetOwnerFromEventUseCase::class.java
        )
            .newInstance(
                getEventFromIdUseCase,
                getOwnerFromEventUseCase
            )
    }

}