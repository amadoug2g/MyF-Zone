package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.usecases.event.*
import com.myfzone_sport.myf_zone.usecases.user.SignOutUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FragmentViewModel(
    private val getCloseEventsUseCase: GetCloseEventsUseCase,
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getFriendlyEventsUseCase: GetFriendlyEventsUseCase,
    private val getTourneyEventsUseCase: GetTourneyEventsUseCase,
    private val getPlateauEventsUseCase: GetPlateauEventsUseCase,
    private val getUserEventsUseCase: GetUserEventsUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    //region Variables
    private val _closeEventsList = MutableLiveData<MutableList<Event>>()
    val closeEventsList = _closeEventsList

    private val _allEventsList = MutableLiveData<MutableList<Event>>()
    val allEventsList = _allEventsList

    private val _friendlyEventsList = MutableLiveData<MutableList<Event>>()
    val friendlyEventsList = _friendlyEventsList

    private val _tourneyEventsList = MutableLiveData<MutableList<Event>>()
    val tourneyEventsList = _tourneyEventsList

    private val _plateauEventsList = MutableLiveData<MutableList<Event>>()
    val plateauEventsList = _plateauEventsList

    private val _userEventsList = MutableLiveData<MutableList<Event>>()
    val userEventsList = _userEventsList

    val isUserConnected = MutableLiveData(false)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    init {
        try {
            getAllEvents()
            getFriendlyEvents()
            getTourneyEvents()
            getPlateauEvents()
        } catch (e: Exception) {
            onResult(e.message.toString())
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
                        val message = "All events fetching failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getCloseEvents() {
        viewModelScope.launch(IO) {
            getCloseEventsUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        _closeEventsList.postValue(state.data)
                    }
                    is State.Failed -> {
                        val message = "Close events fetching failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getUserEvents() {
        viewModelScope.launch(IO) {
            getUserEventsUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        _userEventsList.postValue(state.data)
                    }
                    is State.Failed -> {
                        val message = "User events fetching failure: ${state.message}"
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
                        val message = "Friendly events fetching failure: ${state.message}"
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
                        val message = "Tourney events fetching failure: ${state.message}"
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
                        val message = "Plateau events fetching failure: ${state.message}"
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

class FragmentViewModelFactory(
    private val getCloseEventsUseCase: GetCloseEventsUseCase,
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getFriendlyEventsUseCase: GetFriendlyEventsUseCase,
    private val getTourneyEventsUseCase: GetTourneyEventsUseCase,
    private val getPlateauEventsUseCase: GetPlateauEventsUseCase,
    private val getUserEventsUseCase: GetUserEventsUseCase,
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
            SignOutUseCase::class.java
        )
            .newInstance(
                getCloseEventsUseCase,
                getAllEventsUseCase,
                getFriendlyEventsUseCase,
                getTourneyEventsUseCase,
                getPlateauEventsUseCase,
                getUserEventsUseCase,
                signOutUseCase
            )
    }

}