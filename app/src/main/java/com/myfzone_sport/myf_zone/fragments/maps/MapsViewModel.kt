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
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.club.Club
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.model.event.EventOwner
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

    private val _owner = MutableLiveData<EventOwner>()
    val owner: LiveData<EventOwner>
        get() = _owner

    private val _marker = MutableLiveData<Marker>()
    val marker: LiveData<Marker>
        get() = _marker

    private val _item = MutableLiveData<MyClusterItem>()
    val item: LiveData<MyClusterItem>
        get() = _item

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

    fun initializeMap(map: GoogleMap) {
        viewModelScope.launch {
            MapsService.initializeMap(map, ManagerAuth.activeCoachClub)
        }
    }

    fun getEvents() = MapsService.getEvents(startDate.value!!, endDate.value!!)

    private suspend fun getOwner() = MapsService.getOwnerFromEvent(eventId.value!!)

    fun getOwnerFromEvent() {
        viewModelScope.launch {
            _owner.value = getOwner()
        }
    }

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
}