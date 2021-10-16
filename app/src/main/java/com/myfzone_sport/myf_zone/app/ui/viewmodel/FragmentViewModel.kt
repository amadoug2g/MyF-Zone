package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.app.framework.FirebaseService
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClub
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliation
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliationLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.firebaseInstance
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.fragments.profile.ProfileService
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.usecases.event.*
import com.myfzone_sport.myf_zone.usecases.user.*
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FragmentViewModel(
    private val getCloseEventsUseCase: GetCloseEventsUseCase,
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getFriendlyEventsUseCase: GetFriendlyEventsUseCase,
    private val getTourneyEventsUseCase: GetTourneyEventsUseCase,
    private val getPlateauEventsUseCase: GetPlateauEventsUseCase,
    private val getUserEventsUseCase: GetUserEventsUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getUserClubUseCase: GetUserClubUseCase,
    private val getUserAffiliationUseCase: GetUserClubAffiliationUseCase,
    private val getUserEventListUseCase: GetUserEventListUseCase,
    private val signOutUseCase: SignOutUseCase
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

    private val _friendlyEventsList = MutableLiveData<MutableList<Event>>()
    val friendlyEventsList: LiveData<MutableList<Event>> = _friendlyEventsList

    private val _tourneyEventsList = MutableLiveData<MutableList<Event>>()
    val tourneyEventsList: LiveData<MutableList<Event>> = _tourneyEventsList

    private val _plateauEventsList = MutableLiveData<MutableList<Event>>()
    val plateauEventsList: LiveData<MutableList<Event>> = _plateauEventsList

    private val _userEventsList = MutableLiveData<MutableList<Event>>()
    val userEventsList: LiveData<MutableList<Event>> = _userEventsList

    val isUserConnected = MutableLiveData(false)
    val isUserAffiliated = MutableLiveData(false)
    val closeEventQuery: MutableLiveData<CollectionReference> = MutableLiveData()

    private val _userImagePath = MutableLiveData<StorageReference>()
    val userImagePath: LiveData<StorageReference> = _userImagePath

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

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

    //region Events
//    fun getQuery() {
//        closeEventQuery.postValue(firebaseInstance
//            .collection(COACH_PATH)
//            .document(coach.value!!.id)
//            .collection("ClubAffiliation")
//            .document(activeCoachClubAffiliation!!.clubId)
//            .collection("CoachEvent"))
//    }

    fun getQuery(): CollectionReference {
        return firebaseInstance
            .collection(COACH_PATH)
            .document(activeCoachLive.value!!.id)
            .collection("ClubAffiliation")
            .document(activeCoachClubAffiliationLive.value!!.clubId)
            .collection("CoachEvent")
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

    fun getCloseEvents() {
        viewModelScope.launch {
            getCloseEventsUseCase.invoke()
//                .stateIn(viewModelScope)
//                .shareIn(viewModelScope, SharingStarted.Lazily, 3)
                .collect { state ->
                    when (state) {
                        is State.Loading -> {
                            startLoading()
                        }
                        is State.Success -> {
                            onResult()
                            _closeEventsList.postValue(state.data)
                        }
                        is State.Failed -> {
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
//                .shareIn(viewModelScope, SharingStarted.Lazily, 3)
                .collect { state ->
                    when (state) {
                        is State.Loading -> {
                            startLoading()
                        }
                        is State.Success -> {
                            onResult()
                            _userEventsList.postValue(state.data)
                        }
                        is State.Failed -> {
                            val message = "User events fetching failed: ${state.message}"
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
    //endregion

    //region User
    fun getImageReference(path: String) {
        viewModelScope.launch(IO) {
            _userImagePath.postValue(getImageReferenceUseCase.invoke(path))
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
                        activeCoach = state.data
                        activeCoachLive.postValue(state.data)
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

    fun getUserClub() {
        viewModelScope.launch(IO) {
            getUserClubUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                        activeCoachClub = state.data
                        activeCoachClubLive.postValue(state.data)
                        _coachClub.postValue(state.data)
                    }
                    is State.Failed -> {
                        val message = "Club update failed: ${state.message}"
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
                        activeCoachClubAffiliation = state.data
                        activeCoachClubAffiliationLive.postValue(state.data)
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

    fun getUserEventList() {
        viewModelScope.launch(IO) {
            getUserEventListUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "User events fetching failed: ${state.message}"
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
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getUserClubUseCase: GetUserClubUseCase,
    private val getUserAffiliationUseCase: GetUserClubAffiliationUseCase,
    private val getUserEventListUseCase: GetUserEventListUseCase,
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
            GetImageReferenceUseCase::class.java,
            GetUserUseCase::class.java,
            GetUserClubUseCase::class.java,
            GetUserClubAffiliationUseCase::class.java,
            GetUserEventListUseCase::class.java,
            SignOutUseCase::class.java
        )
            .newInstance(
                getCloseEventsUseCase,
                getAllEventsUseCase,
                getFriendlyEventsUseCase,
                getTourneyEventsUseCase,
                getPlateauEventsUseCase,
                getUserEventsUseCase,
                getImageReferenceUseCase,
                getUserUseCase,
                getUserClubUseCase,
                getUserAffiliationUseCase,
                getUserEventListUseCase,
                signOutUseCase
            )
    }

}