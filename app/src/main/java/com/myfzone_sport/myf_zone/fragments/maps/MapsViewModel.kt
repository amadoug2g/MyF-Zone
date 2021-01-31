package com.myfzone_sport.myf_zone.fragments.maps

import android.content.Context
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.club.Club
import com.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.model.coach.Coach
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.model.maps.MyClusterItem
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by Amadou on 06/12/2020, 19:11
 *
 * Maps ViewModel class
 *
 */

class MapsViewModel : ViewModel() {
    private val TAG = MapsViewModel::class.java.simpleName

    //region Variables
    private val _isUserSignedIn = MutableLiveData<Boolean>(false)
    val isUserSignedIn: LiveData<Boolean>
        get() = _isUserSignedIn

    private val _isUserAffiliated = MutableLiveData<Boolean>(false)
    val isUserAffiliated: LiveData<Boolean>
        get() = _isUserAffiliated

    private val _isMapInitialized = MutableLiveData<Boolean>(false)
    val isMapInitialized: LiveData<Boolean>
        get() = _isMapInitialized

    private val _eventList = MutableLiveData<MutableList<Event>>(mutableListOf())
    val eventList: LiveData<MutableList<Event>>
        get() = _eventList

    private var context = MutableLiveData<Context>()
    var map = MutableLiveData<GoogleMap>()

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

    private val _coach = MutableLiveData<Coach>()
    val coach: LiveData<Coach>
        get() = _coach

    private val _club = MutableLiveData<Club>()
    val club: LiveData<Club>
        get() = _club

    private val _marker = MutableLiveData<Marker>()
    val marker: LiveData<Marker>
        get() = _marker

    private val _item = MutableLiveData<MyClusterItem>()
    val item: LiveData<MyClusterItem>
        get() = _item

    private val _clubAffiliation = MutableLiveData<ClubAffiliation>()
    private val clubAffiliation: LiveData<ClubAffiliation>
        get() = _clubAffiliation

    private val _eventId = MutableLiveData<String>()
    val eventId: LiveData<String>
        get() = _eventId

    var filterCount = MutableLiveData(0)
    var filterCountFriendly = MutableLiveData(0)
    var filterCountTourney = MutableLiveData(0)
    var filterCountPlateau = MutableLiveData(0)
    var startDate = MutableLiveData<Long>(Calendar.getInstance().timeInMillis)
    var endDate = MutableLiveData<Long>(Calendar.getInstance().timeInMillis + (604800000))
    //endregion

    init {
        _isUserSignedIn.value = checkUserSignedIn()
    }

    private fun checkUserSignedIn(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return (currentUser != null)
    }

    fun assignContext(ctx: Context) {
        context.value = ctx
    }

    fun assignStartDate(time: Long) {
        startDate.value = time
    }

    fun assignEndDate(time: Long) {
        endDate.value = time
    }

    fun assignMap(googleMap: GoogleMap) {
        map.value = googleMap
    }

    fun assignMarker(marker: Marker) {
        _marker.value = marker
    }

    fun assignItem(item: MyClusterItem) {
        _item.value = item
    }

    fun assignFilterCount(counter: Int) {
        filterCount.value = counter
    }

    fun assignFilterFriendlyCount(counter: Int) {
        filterCountFriendly.value = counter
    }

    fun assignFilterTourneyCount(counter: Int) {
        filterCountTourney.value = counter
    }

    fun assignFilterPlateauCount(counter: Int) {
        filterCountPlateau.value = counter
    }

    fun mapInit() {
        _isMapInitialized.value = _isMapInitialized.value != true
    }

    fun eventInList(eventId: String): Boolean {
        if (!eventList.value.isNullOrEmpty())
            eventList.value?.forEach { event ->
                if (eventId == event.id) return true
            }
        return false
    }

    private fun getCurrentUser() = MapsService.getCurrentUser()

    suspend fun assignUser() {
        getCurrentUser().collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _coach.value = state.data
                    Log.i(TAG, "assignUser Success")
                }
                is State.Failed -> {
                    val bundleTracking = bundleOf("Map Error [assignUser]" to state.message)
                    Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                    val message = "assignUser Failed: ${state.message}"
                    Log.i(TAG, message)
                }
            }
        }
    }

    private fun getUserAffiliation() = MapsService.getUserClub()

    suspend fun assignClubAffiliation() {
        getUserAffiliation().collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _clubAffiliation.value = state.data
                }
                is State.Failed -> {
                    val bundleTracking =
                        bundleOf("Map Error [assignClubAffiliation]" to state.message)
                    Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                    val message = "An error occurred [in assignClub]: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

    private fun getUserClub(clubAffiliation: ClubAffiliation) =
        MapsService.getClubById(clubAffiliation)

    suspend fun assignClub() {
        getUserClub(clubAffiliation.value!!).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _club.value = state.data
                }
                is State.Failed -> {
                    val bundleTracking = bundleOf("Map Error [assignClub]" to state.message)
                    Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                    val message = "An error occurred [in assignClub]: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

    private fun affiliationStatus() = MapsService.checkAffiliationStatus()

    fun initializeMap(map: GoogleMap) {
        viewModelScope.launch {
            MapsService.initializeMap(map, club.value)
        }
    }

    fun getEvents() = MapsService.getEvents(startDate.value!!, endDate.value!!)

    fun addEventListener(onListen: (MutableList<Event>) -> Unit) =
        MapsService.addEventListener(startDate.value!!, endDate.value!!, onListen)

    fun assignEventList() {
        viewModelScope.launch {
            getEvents().collect { state ->
                when (state) {
                    is State.Loading -> {
                        //loadingMsgStart()
                    }
                    is State.Success -> {
                        //loadingMsgEnd()
                        val list = state.data
                        _eventList.value = list
                    }
                    is State.Failed -> {
                        val message = "An error occurred [in assignEventList]: ${state.message}"
                        Log.i(TAG, message)
                    }
                }
            }
        }
    }

    fun assignEventList(list: MutableList<Event>) {
        _eventList.value = list
    }

    fun placeEvents(
        map: GoogleMap,
        context: Context,
        eventList: MutableList<Event>
    ) = MapsService.placeEvents(map, context, eventList)

    fun placeEventsCluster(
        map: GoogleMap,
        context: Context,
        eventList: MutableList<Event>
    ) = MapsService.placeEventsCluster(map, context, eventList)

    fun placeUserClub(club: Club, map: GoogleMap, context: Context) =
        MapsService.placeUserClub(club, map, context)

    private fun getEventById(eventId: String) = MapsService.getEventById(eventId)

    fun assignEventId(marker: Marker) {
        _eventId.value = marker.tag as String
    }

    fun assignEventId(item: MyClusterItem) {
        _eventId.value = item.tag
    }

    fun assignEvent(eventId: String) {
        viewModelScope.launch {
            getEventById(eventId).collect { state ->
                when (state) {
                    is State.Loading -> {
                    }
                    is State.Success -> {
                        val event = state.data
                        _event.value = event
                    }
                    is State.Failed -> {
                        val bundleTracking = bundleOf("Map Error [assignEvent]" to state.message)
                        Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                        val message = "An error occurred [in assignEvent]: ${state.message}"
                        Log.i(TAG, message)
                    }
                }
            }
        }
    }

    suspend fun checkUserAffiliationStatus() {
        affiliationStatus().collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
//                    hideProgressBar()
                    _isUserAffiliated.value = state.data
                }
                is State.Failed -> {
                    _isUserAffiliated.value = false
                    val message =
                        "An error occurred [in checkUserAffiliationStatus]: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
//                    hideProgressBar()
                }
            }
        }
    }
}