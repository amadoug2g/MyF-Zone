package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.location.Location
import androidx.lifecycle.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.usecases.detailevent.GetOwnerFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.event.GetAllEventsUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserClubAffiliationUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserClubUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserEventListUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserUseCase
import com.myfzone_sport.myf_zone.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Created by Amadou on 10/11/2021, 18:58
 */

class MapsViewModel(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getUserEventListUseCase: GetUserEventListUseCase,
    private val getUserClubUseCase: GetUserClubUseCase,
    private val getUserAffiliationUseCase: GetUserClubAffiliationUseCase,
    private val getOwnerFromEventUseCase: GetOwnerFromEventUseCase
) : ViewModel() {
    //region Variables
    private val _isMapInitialized = MutableLiveData(false)
    val isMapInitialized: LiveData<Boolean> = _isMapInitialized

    private val _coach = MutableLiveData<Coach>()
    val coach: LiveData<Coach> = _coach

    private val _coachAffiliation = MutableLiveData<ClubAffiliation>()

    private val _coachClub = MutableLiveData<Club>()

    private val _closeEventsList = MutableLiveData<MutableList<Event>>()
    val closeEventsList: LiveData<MutableList<Event>> = _closeEventsList

    private val _map = MutableLiveData<GoogleMap>()
    val map: LiveData<GoogleMap> = _map

    private val _userEventsList = MutableLiveData<MutableList<Event>>()
    val userEventsList: LiveData<MutableList<Event>> = _userEventsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    init {
//        initializeHome()
    }

    fun initializeHome() {
//        getAllEvents()
    }

    fun assignMap(map: GoogleMap, club: Club) {
        _map.postValue(map)
        _coachClub.postValue(club)
        _isMapInitialized.postValue(true)
        initializeMap(map, club)
        getAllEvents(club)
    }

    private fun initializeMap(map: GoogleMap, club: Club) {
        map.uiSettings.apply {
            isCompassEnabled = false
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = true
        }

        map.apply {
            setPadding(0, 0, 0, 146)

            setMinZoomPreference(2f)
            setMaxZoomPreference(16f)

            val positionClub = LatLng(club.lat, club.lng)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(positionClub, 3f))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(positionClub, 13.5f))
        }
    }

    private fun getAllEvents(club: Club) {
        viewModelScope.launch(Dispatchers.IO) {
            getAllEventsUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val eventList = state.data

                        getUserEventList(eventList, club)
//                        getUser(eventList)
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

    private fun getUser(list: MutableList<Event>) {
        viewModelScope.launch {
            getUserUseCase.load().stateIn(viewModelScope).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val coach = state.data

                        _coach.postValue(coach)
                        getUserAffiliation(list, coach)
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

    private fun getUserAffiliation(list: MutableList<Event>, coach: Coach) {
        viewModelScope.launch(Dispatchers.IO) {
            getUserAffiliationUseCase.invoke(coach).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val affiliation = state.data!!

                        _coachAffiliation.postValue(affiliation)

                        getUserClub(list, affiliation)
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

    private fun getUserClub(list: MutableList<Event>, clubAffiliation: ClubAffiliation) {
        viewModelScope.launch(Dispatchers.IO) {
            getUserClubUseCase.invoke(clubAffiliation).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        state.data?.let { club ->
                            getUserEventList(list, club)
                            _coachClub.postValue(club)
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
        viewModelScope.launch(Dispatchers.IO) {
            getUserEventListUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val coachEventList = state.data

                        sortOwnerEvents(list, club, coachEventList)
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

        viewModelScope.launch {
            assignOwners(result)
        }
    }

    private suspend fun assignOwners(list: MutableList<Event>) {
        val result = viewModelScope.async {
            for (event in list) {
                val mEventOwnerQuery = Constants.DB
                    .collection(Constants.EVENT_PATH + "/${event.id}/Owner")

                val snapshot = mEventOwnerQuery.get().await().documents[0]

                snapshot.toObject(EventOwner::class.java)?.let {
                    event.owner = it
                }
            }
            return@async list
        }.await()

        _closeEventsList.postValue(result)
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

class MapsViewModelFactory(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getUserEventListUseCase: GetUserEventListUseCase,
    private val getUserClubUseCase: GetUserClubUseCase,
    private val getUserAffiliationUseCase: GetUserClubAffiliationUseCase,
    private val getOwnerFromEventUseCase: GetOwnerFromEventUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetAllEventsUseCase::class.java,
            GetUserUseCase::class.java,
            GetUserEventListUseCase::class.java,
            GetUserClubUseCase::class.java,
            GetUserClubAffiliationUseCase::class.java,
            GetOwnerFromEventUseCase::class.java,
        )
            .newInstance(
                getAllEventsUseCase,
                getUserUseCase,
                getUserEventListUseCase,
                getUserClubUseCase,
                getUserAffiliationUseCase,
                getOwnerFromEventUseCase
            )
    }

}