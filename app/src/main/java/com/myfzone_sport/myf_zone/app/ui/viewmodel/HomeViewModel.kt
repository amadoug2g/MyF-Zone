package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.app.framework.FirebaseService
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliationLive
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.usecases.event.*
import com.myfzone_sport.myf_zone.usecases.user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 12/10/2021, 16:34
 */

class HomeViewModel(
    private val getCloseEventsUseCase: GetCloseEventsUseCase,
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getFriendlyEventsUseCase: GetFriendlyEventsUseCase,
    private val getTourneyEventsUseCase: GetTourneyEventsUseCase,
    private val getPlateauEventsUseCase: GetPlateauEventsUseCase,
    private val getUserEventsUseCase: GetUserEventsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getUserClubUseCase: GetUserClubUseCase,
    private val getUserAffiliationUseCase: GetUserClubAffiliationUseCase,
    private val signOutUseCase: SignOutUseCase
): ViewModel() {

    //region Variables
    private val _coach = MutableLiveData<Coach>()
    val coach: LiveData<Coach> = _coach

    private val _coachAffiliation = MutableLiveData<ClubAffiliation>()
    val coachAffiliation: LiveData<ClubAffiliation> = _coachAffiliation

    private val _allEventsList = MutableLiveData<MutableList<Event>>()
    val allEventsList: LiveData<MutableList<Event>> = _allEventsList

    private val _closeEventsList = MutableLiveData<MutableList<Event>>()
    val closeEventsList: LiveData<MutableList<Event>> = _closeEventsList

    private val _friendlyEventsList = MutableLiveData<MutableList<Event>>()
    val friendlyEventsList: LiveData<MutableList<Event>> = _friendlyEventsList

    private val _tourneyEventsList = MutableLiveData<MutableList<Event>>()
    val tourneyEventsList: LiveData<MutableList<Event>> = _tourneyEventsList

    private val _plateauEventsList = MutableLiveData<MutableList<Event>>()
    val plateauEventsList: LiveData<MutableList<Event>> = _plateauEventsList

    private val _userEventsList = MutableLiveData<MutableList<Event>>()
    val userEventsList: LiveData<MutableList<Event>> = _userEventsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    val isUserConnected = MutableLiveData(false)
    //endregion

    //region Functions
    init {
        try {
            getUser()
            getUserAffiliation()
            getFriendlyEvents()
            getTourneyEvents()
            getPlateauEvents()
            getCloseEvents()
            getUserEvents()
        } catch (e: Exception) {
            onResult(e.localizedMessage.toString())
        }
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
                    }
                    is State.Failed -> {
                        val message = "All events fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getFriendlyEvents() {
        viewModelScope.launch(IO) {
            getFriendlyEventsUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        _friendlyEventsList.postValue(state.data)
                    }
                    is State.Failed -> {
                        val message = "Friendly events fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getTourneyEvents() {
        viewModelScope.launch(IO) {
            getTourneyEventsUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        _tourneyEventsList.postValue(state.data)
                    }
                    is State.Failed -> {
                        val message = "Tourney events fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getPlateauEvents() {
        viewModelScope.launch(IO) {
            getPlateauEventsUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        _plateauEventsList.postValue(state.data)
                    }
                    is State.Failed -> {
                        val message = "Plateau events fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getCloseEvents() {
        viewModelScope.launch {
            getCloseEventsUseCase.invoke()
                .stateIn(viewModelScope, SharingStarted.Eagerly, mutableListOf<Event>())
//                .shareIn(viewModelScope, SharingStarted.Eagerly, 3)
                .collect { state ->
                    when (state) {
                        is State.Loading<*> -> {
                            startLoading()
                        }
                        is State.Success<*> -> {
                            onResult()
                            _closeEventsList.postValue(state.data as MutableList<Event>?)
                        }
                        is State.Failed<*> -> {
                            val message = "Close events fetching failed: ${state.message}"
                            onResult(message)
                        }
                    }
                }
        }
    }

    fun getUserEvents() {
        viewModelScope.launch {
            getUserEventsUseCase.invoke()
//                .stateIn(viewModelScope)
                .stateIn(viewModelScope, SharingStarted.Eagerly, mutableListOf<Event>())
//                .shareIn(viewModelScope, SharingStarted.Lazily, 3)
                .collect { state ->
                    when (state) {
                        is State.Loading<*> -> {
                            startLoading()
                        }
                        is State.Success<*> -> {
                            onResult()
//                            _userEventsList.postValue(state.data)
                            _userEventsList.postValue(state.data as MutableList<Event>?)
                        }
                        is State.Failed<*> -> {
                            val message = "User events fetching failed: ${state.message}"
                            onResult(message)
                        }
                    }
                }
        }
    }

    fun getUser() {
        viewModelScope.launch {
            getUserUseCase.load().stateIn(viewModelScope).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        _coach.postValue(state.data)
                    }
                    is State.Failed -> {
                        val message = "User update failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getUserAffiliation() {
        viewModelScope.launch(IO) {
            getUserAffiliationUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        _coachAffiliation.postValue(state.data)
                    }
                    is State.Failed -> {
                        val message = "Affiliation update failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun userConnected() {
        isUserConnected.postValue(true)
    }

    fun userNotConnected() {
        isUserConnected.postValue(false)
    }

    fun signOut() {
        signOutUseCase.invoke()
        userNotConnected()
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
    private val getCloseEventsUseCase: GetCloseEventsUseCase,
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getFriendlyEventsUseCase: GetFriendlyEventsUseCase,
    private val getTourneyEventsUseCase: GetTourneyEventsUseCase,
    private val getPlateauEventsUseCase: GetPlateauEventsUseCase,
    private val getUserEventsUseCase: GetUserEventsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getUserClubUseCase: GetUserClubUseCase,
    private val getUserAffiliationUseCase: GetUserClubAffiliationUseCase,
    private val signOutUseCase: SignOutUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetCloseEventsUseCase::class.java,
            GetAllEventsUseCase::class.java,
            GetFriendlyEventsUseCase::class.java,
            GetTourneyEventsUseCase::class.java,
            GetPlateauEventsUseCase::class.java,
            GetUserEventsUseCase::class.java,
            GetUserUseCase::class.java,
            GetUserClubUseCase::class.java,
            GetUserClubAffiliationUseCase::class.java,
            SignOutUseCase::class.java
        )
            .newInstance(
                getCloseEventsUseCase,
                getAllEventsUseCase,
                getFriendlyEventsUseCase,
                getTourneyEventsUseCase,
                getPlateauEventsUseCase,
                getUserEventsUseCase,
                getUserUseCase,
                getUserClubUseCase,
                getUserAffiliationUseCase,
                signOutUseCase
            )
    }

}