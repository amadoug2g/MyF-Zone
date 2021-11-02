package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachEvents
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.usecases.event.GetAllEventsUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserClubAffiliationUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserClubUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserEventListUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by Amadou on 12/10/2021, 16:34
 */

class HomeViewModel(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getUserEventListUseCase: GetUserEventListUseCase,
    private val getUserClubUseCase: GetUserClubUseCase,
    private val getUserAffiliationUseCase: GetUserClubAffiliationUseCase
) : ViewModel() {

    //region Variables
    private val _coach = MutableLiveData<Coach>()
    val coach: LiveData<Coach> = _coach

    private val _coachAffiliation = MutableLiveData<ClubAffiliation>()
    val coachAffiliation: LiveData<ClubAffiliation> = _coachAffiliation

    private val _coachClub = MutableLiveData<Club>()
    val coachClub: LiveData<Club> = _coachClub

    private val _closeEventsList = MutableLiveData<MutableList<Event>>()
    val closeEventsList: LiveData<MutableList<Event>> = _closeEventsList

    private val _userEventsList = MutableLiveData<MutableList<Event>>()
    val userEventsList: LiveData<MutableList<Event>> = _userEventsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val isUserConnected = MutableLiveData(false)
    //endregion

    //region Functions
    init {
        getUser()
        initializeHome()
    }

    fun initializeHome() {
//        getUserAffiliation()
        getAllEvents()
    }

    private fun getAllEvents() {
        viewModelScope.launch(IO) {
            getAllEventsUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val eventList = state.data

                        getUserClub(eventList)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "All events fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun sortOwnerEvents(
        list: MutableList<Event>,
        club: Club,
        eventList: MutableList<String>
    ) {
        val resultOwner = mutableListOf<Event>()
        val resultNotOwner = mutableListOf<Event>()

        for (event in list)
            if (eventList.contains(event.id)) {
                resultOwner.add(event)
            } else {
                resultNotOwner.add(event)
            }

        _userEventsList.postValue(resultOwner.asReversed())

        getCloseEvents(resultNotOwner, club)
    }

    private fun getCloseEvents(list: MutableList<Event>, club: Club) {
        val result = mutableListOf<Event>()
        val sortedResult = floatArrayOf(12F)
        val tree = TreeMap<Float, Event>()

        for (i in list) {
            Location.distanceBetween(
                club.lat,
                club.lng,
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

    private fun getUser() {
        viewModelScope.launch {
            getUserUseCase.load().stateIn(viewModelScope).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val coach = state.data

                        _coach.postValue(coach)
                        initializeHome()
                        onResult()

//                        if (state.data != null) {
//                            getUserAffiliation()
//                            userConnected()
//                        } else {
//                            userNotConnected()
//                        }
                    }
                    is State.Failed -> {
                        val message = "User update failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getUserAffiliation() {
        viewModelScope.launch(IO) {
            getUserAffiliationUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val affiliation = state.data!!

                        _coachAffiliation.postValue(affiliation)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Affiliation update failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getUserClub(list: MutableList<Event>) {
        viewModelScope.launch(IO) {
            getUserClubUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val club = state.data

                        if (club != null) {
                            _coachClub.postValue(club)
                            getUserEventList(list, club)
                        }
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Club fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getUserEventList(list: MutableList<Event>, club: Club) {
        viewModelScope.launch(IO) {
            getUserEventListUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val coachEventList = state.data

                        sortOwnerEvents(list, club, state.data)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Event list fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun userConnected() {
        isUserConnected.postValue(true)
    }

    private fun userNotConnected() {
        isUserConnected.postValue(false)
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

class HomeViewModelFactory(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getUserEventListUseCase: GetUserEventListUseCase,
    private val getUserClubUseCase: GetUserClubUseCase,
    private val getUserAffiliationUseCase: GetUserClubAffiliationUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetAllEventsUseCase::class.java,
            GetUserUseCase::class.java,
            GetUserEventListUseCase::class.java,
            GetUserClubUseCase::class.java,
            GetUserClubAffiliationUseCase::class.java,
        )
            .newInstance(
                getAllEventsUseCase,
                getUserUseCase,
                getUserEventListUseCase,
                getUserClubUseCase,
                getUserAffiliationUseCase
            )
    }

}