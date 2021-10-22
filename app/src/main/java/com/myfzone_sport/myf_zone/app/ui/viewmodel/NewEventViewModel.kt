package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.*
import com.google.android.libraries.places.api.model.Place
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetEventFromIdUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetOwnerFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.newevent.AddNewEventToUserUseCase
import com.myfzone_sport.myf_zone.usecases.newevent.AddOwnerToEventUseCase
import com.myfzone_sport.myf_zone.usecases.newevent.CreateEventUseCase
import com.myfzone_sport.myf_zone.usecases.newevent.GetOwnerForNewEventUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 19/10/2021, 21:47
 */

class NewEventViewModel(
    private val getOwnerForNewEventUseCase: GetOwnerForNewEventUseCase,
    private val addOwnerToEventUseCase: AddOwnerToEventUseCase,
    private val addNewEventToUserUseCase: AddNewEventToUserUseCase,
    private val createEventUseCase: CreateEventUseCase
): ViewModel() {

    //region Variables
    private val _event = MutableLiveData<Event>()
    val event = _event

    private val _fields = MutableLiveData<MutableList<Place.Field>>()
    val fields: LiveData<MutableList<Place.Field>> = _fields

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
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

    fun getOwner() {
        viewModelScope.launch {
            getOwnerForNewEventUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {}
                    is State.Success -> {}
                    is State.Failed -> {}
                }
            }
        }
    }

    fun addOwnerToEvent(event: Event, owner: EventOwner) {
        viewModelScope.launch {
            addOwnerToEventUseCase.invoke(event, owner).collect { state ->
                when (state) {
                    is State.Loading -> {}
                    is State.Success -> {}
                    is State.Failed -> {}
                }
            }
        }
    }

    fun addEventToUser(event: Event, owner: EventOwner, club: ClubAffiliation) {
        viewModelScope.launch {
            addNewEventToUserUseCase.invoke(event, owner, club).collect { state ->
                when (state) {
                    is State.Loading -> {}
                    is State.Success -> {}
                    is State.Failed -> {}
                }
            }
        }
    }

    fun createEvent(event: Event) {
        viewModelScope.launch {
            createEventUseCase.invoke(event).collect { state ->
                when (state) {
                    is State.Loading -> {}
                    is State.Success -> {}
                    is State.Failed -> {}
                }
            }
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

class NewEventViewModelFactory(
    private val getOwnerForNewEventUseCase: GetOwnerForNewEventUseCase,
    private val addOwnerToEventUseCase: AddOwnerToEventUseCase,
    private val addNewEventToUserUseCase: AddNewEventToUserUseCase,
    private val createEventUseCase: CreateEventUseCase
    ): ViewModelProvider.Factory {
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