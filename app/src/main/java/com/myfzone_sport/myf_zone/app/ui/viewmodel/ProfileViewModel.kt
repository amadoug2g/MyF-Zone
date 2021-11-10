package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.*
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachEvents
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.event.GetAllEventsUseCase
import com.myfzone_sport.myf_zone.usecases.user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 18/10/2021, 20:24
 */

class ProfileViewModel(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getUserClubUseCase: GetUserClubUseCase,
    private val getUserAffiliationUseCase: GetUserClubAffiliationUseCase,
    private val getUserEventListUseCase: GetUserEventListUseCase,
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase,
) : ViewModel() {

    //region Variables
    private val _coach = MutableLiveData<Coach>()
    val coach: LiveData<Coach> = _coach

    private val _coachClub = MutableLiveData<Club>()
    val coachClub: LiveData<Club> = _coachClub

    private val _coachAffiliation = MutableLiveData<ClubAffiliation>()
    val coachAffiliation: LiveData<ClubAffiliation> = _coachAffiliation

    private val _closeEventsList = MutableLiveData<MutableList<Event>>()
    val closeEventsList: LiveData<MutableList<Event>> = _closeEventsList

    private val _allEventsList = MutableLiveData<MutableList<Event>>()
    val allEventsList: LiveData<MutableList<Event>> = _allEventsList

    private val _allEventsNotOwnedList = MutableLiveData<MutableList<Event>>()
    val allEventsNotOwnedList: LiveData<MutableList<Event>> = _allEventsNotOwnedList

    private val _participationList = MutableLiveData<MutableList<Event>>()
    val participationList: LiveData<MutableList<Event>> = _participationList

    private val _userEventsList = MutableLiveData<MutableList<Event>>()
    val userEventsList: LiveData<MutableList<Event>> = _userEventsList

    private val _userImagePath = MutableLiveData<StorageReference>()
    val userImagePath: LiveData<StorageReference> = _userImagePath

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    init {
        getUser()
    }

    private fun getUser() {
        viewModelScope.launch {
            getUserUseCase.load().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        state.data.apply {
                            _coach.postValue(this)
                            getUserClubAffiliation(this)
                            getAllEvents(this)
                        }

                        onResult()
                    }
                    is State.Failed -> {
                        val message = "User update failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getUserClubAffiliation(coach: Coach) {
        viewModelScope.launch {
            getUserAffiliationUseCase.invoke(coach).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        state.data?.let { affiliation ->
                            _coachAffiliation.postValue(affiliation)
                            getUserClub(affiliation)
                        }
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

    private fun getUserClub(clubAffiliation: ClubAffiliation) {
        viewModelScope.launch {
            getUserClubUseCase.invoke(clubAffiliation).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        state.data?.let { club ->
                            _coachClub.postValue(club)
                        }
                    }
                    is State.Failed -> {
                        val message = "Club update failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getAllEvents(coach: Coach) {
        viewModelScope.launch(Dispatchers.IO) {
            getAllEventsUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val eventList = state.data

                        _allEventsList.postValue(eventList)
                        getAllEventsNotOwned(coach, eventList)
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

    private fun getAllEventsNotOwned(coach: Coach, list: MutableList<Event>) {
        val resultOwner = mutableListOf<Event>()
        val resultNotOwner = mutableListOf<Event>()

        for (event in list)
            if (isUserOwner(event.id)) {
                resultOwner.add(event)
            } else {
                resultNotOwner.add(event)
            }

        getParticipationList(coach, resultNotOwner)

        _allEventsNotOwnedList.postValue(resultNotOwner)
        _userEventsList.postValue(resultOwner.asReversed())
    }

    private fun getParticipationList(coach: Coach, list: MutableList<Event>) {
        val result = mutableListOf<Event>()
        var listId: MutableList<String>

        for (event in list) {
            viewModelScope.launch {
                getAllParticipantsFromEventUseCase.invoke(event.id).collect { state ->
                    when (state) {
                        is State.Loading -> {
                            startLoading()
                        }
                        is State.Success -> {
                            onResult()
                            listId = mutableListOf()
                            for (i in state.data) listId.add(i.coachId)
//                            if (listId.contains(coach.id)) _participationList.value?.add(event)
                            if (listId.contains(coach.id)) result.add(event)
                        }
                        is State.Failed -> {
                            val message = "All events fetching failed: ${state.message}"
                            onResult(message)
                        }
                    }
                }
            }
        }

        _participationList.postValue(result)
    }

    fun getUserEvents(list: MutableList<Event>) {
        val result = mutableListOf<Event>()
        for (event in list)
            if (isUserOwner(event.id)) result.add(event)

        _userEventsList.postValue(result)
    }

    fun getImageReference(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _userImagePath.postValue(getImageReferenceUseCase.invoke(path))
        }
    }

    private fun isUserOwner(eventId: String): Boolean {
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

class ProfileViewModelFactory(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getUserClubUseCase: GetUserClubUseCase,
    private val getUserAffiliationUseCase: GetUserClubAffiliationUseCase,
    private val getUserEventListUseCase: GetUserEventListUseCase,
    private val getAllParticipantsFromEventUseCase: GetAllParticipantsFromEventUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetAllEventsUseCase::class.java,
            GetImageReferenceUseCase::class.java,
            GetUserUseCase::class.java,
            GetUserClubUseCase::class.java,
            GetUserClubAffiliationUseCase::class.java,
            GetUserEventListUseCase::class.java,
            GetAllParticipantsFromEventUseCase::class.java
        )
            .newInstance(
                getAllEventsUseCase,
                getImageReferenceUseCase,
                getUserUseCase,
                getUserClubUseCase,
                getUserAffiliationUseCase,
                getUserEventListUseCase,
                getAllParticipantsFromEventUseCase
            )
    }

}