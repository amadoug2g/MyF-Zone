package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.location.Location
import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClub
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliation
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachEvents
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.usecases.event.GetAllEventsUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by Amadou on 14/10/2021, 18:16
 */

class CategoryListViewModel(
    private val getAllEventsUseCase: GetAllEventsUseCase,
): ViewModel() {

    //region Variables
    private val _allEventsList = MutableLiveData<MutableList<Event>>()
    val allEventsList: LiveData<MutableList<Event>> = _allEventsList

    private val _allEventsNotOwnedList = MutableLiveData<MutableList<Event>>()
    val allEventsNotOwnedList: LiveData<MutableList<Event>> = _allEventsNotOwnedList

    private val _closeEventsList = MutableLiveData<MutableList<Event>>()
    val closeEventsList: LiveData<MutableList<Event>> = _closeEventsList

    private val _friendlyEventsList = MutableLiveData<MutableList<Event>>()
    val friendlyEventsList: LiveData<MutableList<Event>> = _friendlyEventsList

    private val _friendlyEventsNotOwnedList = MutableLiveData<MutableList<Event>>()
    val friendlyEventsNotOwnedList: LiveData<MutableList<Event>> = _friendlyEventsNotOwnedList

    private val _tourneyEventsList = MutableLiveData<MutableList<Event>>()
    val tourneyEventsList: LiveData<MutableList<Event>> = _tourneyEventsList

    private val _tourneyEventsNotOwnedList = MutableLiveData<MutableList<Event>>()
    val tourneyEventsNotOwnedList: LiveData<MutableList<Event>> = _tourneyEventsNotOwnedList

    private val _plateauEventsList = MutableLiveData<MutableList<Event>>()
    val plateauEventsList: LiveData<MutableList<Event>> = _plateauEventsList

    private val _plateauEventsNotOwnedList = MutableLiveData<MutableList<Event>>()
    val plateauEventsNotOwnedList: LiveData<MutableList<Event>> = _plateauEventsNotOwnedList

    private val _userEventsList = MutableLiveData<MutableList<Event>>()
    val userEventsList: LiveData<MutableList<Event>> = _userEventsList

    private val _isUserConnected = MutableLiveData(false)
    val isUserConnected: LiveData<Boolean> = _isUserConnected

    private val _isUserAffiliated = MutableLiveData(false)
    val isUserAffiliated: LiveData<Boolean> = _isUserAffiliated

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    init {
        getAllEvents()
        getUserStatus()
    }

    private fun getUserStatus() {
        _isUserConnected.postValue(activeCoach != null)
        _isUserAffiliated.postValue(activeCoachClubAffiliation != null)
    }

    private fun getAllEvents() {
        viewModelScope.launch(IO) {
            getAllEventsUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        _allEventsList.postValue(state.data)
//                        sortOwnerEvents(state.data)
                        getAllEventsNotOwned(state.data)
                        getFriendlyEvents(state.data)
                        getFriendlyEventsNotOwned(state.data)
                        getTourneyEvents(state.data)
                        getTourneyEventsNotOwned(state.data)
                        getPlateauEvents(state.data)
                        getPlateauEventsNotOwned(state.data)
                    }
                    is State.Failed -> {
                        val message = "All events fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun sortOwnerEvents(list: MutableList<Event> = allEventsList.value!!) {
        val resultOwner = mutableListOf<Event>()
        val resultNotOwner = mutableListOf<Event>()

        for (event in list)
            if (!isUserOwner(event.id)) {
                resultOwner.add(event)
            } else {
                resultNotOwner.add(event)
            }

        _userEventsList.postValue(resultOwner)

        getCloseEvents(resultNotOwner)
    }

    private fun getAllEventsNotOwned(list: MutableList<Event>) {
        val result = mutableListOf<Event>()
        for (event in list)
            if (!isUserOwner(event.id)) result.add(event)

        _allEventsNotOwnedList.postValue(result)
    }

    fun getCloseEvents(list: MutableList<Event>) {
        val result = mutableListOf<Event>()
        val sortedResult = floatArrayOf(12F)
        val tree = TreeMap<Float, Event>()

        for (i in list) {
            Location.distanceBetween(
                activeCoachClub!!.lat,
                activeCoachClub!!.lng,
                i.lat,
                i.lng, sortedResult
            )

            tree[sortedResult[0]] = i
        }

        for (j in tree) {
            result.add(j.value)
        }

        _closeEventsList.postValue(result)
    }

    fun getUserEvents(list: MutableList<Event>) {
        val result = mutableListOf<Event>()
        for (event in list)
            if (isUserOwner(event.id)) result.add(event)

        _userEventsList.postValue(result)
    }

    private fun getFriendlyEvents(list: MutableList<Event>) {
        val result = mutableListOf<Event>()
        for (event in list)
            if (event.type == "friendly") result.add(event)

        _friendlyEventsList.postValue(result)
    }

    private fun getFriendlyEventsNotOwned(list: MutableList<Event>) {
        val result = mutableListOf<Event>()
        for (event in list)
            if (event.type == "friendly" && !isUserOwner(event.id)) result.add(event)

        _friendlyEventsNotOwnedList.postValue(result)
    }

    private fun getTourneyEvents(list: MutableList<Event>) {
        val result = mutableListOf<Event>()
        for (event in list)
            if (event.type == "tournament") result.add(event)

        _tourneyEventsList.postValue(result)
    }

    private fun getTourneyEventsNotOwned(list: MutableList<Event>) {
        val result = mutableListOf<Event>()
        for (event in list)
            if (event.type == "tournament" && !isUserOwner(event.id)) result.add(event)

        _tourneyEventsNotOwnedList.postValue(result)
    }

    private fun getPlateauEvents(list: MutableList<Event>) {
        val result = mutableListOf<Event>()
        for (event in list)
            if (event.type == "plateau") result.add(event)

        _plateauEventsList.postValue(result)
    }

    private fun getPlateauEventsNotOwned(list: MutableList<Event>) {
        val result = mutableListOf<Event>()
        for (event in list)
            if (event.type == "plateau" && !isUserOwner(event.id)) result.add(event)

        _plateauEventsNotOwnedList.postValue(result)
    }

    private fun isUserOwner(eventId: String): Boolean {
        return (activeCoachEvents.contains(eventId))
    }

    private fun isUserParticipant(eventId: String): Boolean {
        return (activeCoachEvents.contains(eventId))
    }

    private fun isCoachOwner(eventId: String): Boolean {
        return (activeCoachEvents.contains(eventId))
    }

    private fun isCoachParticipant(eventId: String): Boolean {
        return (activeCoachEvents.contains(eventId))
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

class CategoryListViewModelFactory(
    private val getAllEventsUseCase: GetAllEventsUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetAllEventsUseCase::class.java
        )
            .newInstance(
                getAllEventsUseCase
            )
    }
}