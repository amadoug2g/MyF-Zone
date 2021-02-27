package com.myfzone_sport.myf_zone.fragments.event.event_details.owner

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.CollectionReference
import com.myfzone_sport.myf_zone.fragments.event.event_details.EventDetailsService
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.chat.MessagingService
import com.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.model.coach.Coach
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.model.event.EventOwner
import com.myfzone_sport.myf_zone.model.event.EventParticipant
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 31/01/2021, 17:42
 *
 * TODO: File Description
 *
 */
class EventDetailsOwnerViewModel : ViewModel() {
    private val TAG = this::class.java.simpleName

    //region variable declaration
    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

    private val _club = MutableLiveData<ClubAffiliation>()
    val club: LiveData<ClubAffiliation>
        get() = _club

    private val _owner = MutableLiveData<EventOwner>()
    val owner: LiveData<EventOwner>
        get() = _owner

    private val _ownerToken = MutableLiveData<MutableList<String>>()
    val ownerToken: LiveData<MutableList<String>>
        get() = _ownerToken

    private val _coach = MutableLiveData<Coach>()
    val coach: LiveData<Coach>
        get() = _coach

    private val _participantList = MutableLiveData<MutableList<EventParticipant>>()
    val participantList: LiveData<MutableList<EventParticipant>>
        get() = _participantList

    private val _isUserAffiliated = MutableLiveData<Boolean>(false)
    val isUserAffiliated: LiveData<Boolean>
        get() = _isUserAffiliated

    private val _isUserSignedIn = MutableLiveData<Boolean>(false)
    val isUserSignedIn: LiveData<Boolean>
        get() = _isUserSignedIn

    //checks if the user is part of the initial list of participant
    private val _isUserParticipating = MutableLiveData<Boolean>(false)
    val isUserParticipating: LiveData<Boolean>
        get() = _isUserParticipating

    private val _isUserOwner = MutableLiveData<Boolean>(false)
    val isUserOwner: LiveData<Boolean>
        get() = _isUserOwner

    //checks if the user is added as a participant to the event
    private val _isUserParticipant = MutableLiveData<Boolean>(false)
    val isUserParticipant: LiveData<Boolean>
        get() = _isUserParticipant

    val eventId = MutableLiveData<String>()
    //endregion variable declaration

    fun getQuery(eventId: String): CollectionReference {
        return EventDetailsOwnerService.fireStoreInstance
            .collection(EVENT_PATH)
            .document(eventId)
            .collection("Participant")
    }

    fun getEvent(eventId: String) = EventDetailsOwnerService.getEvent(eventId)

    fun getOwnerFromEvent(eventId: String) = EventDetailsOwnerService.getOwnerFromEvent(eventId)

    private fun getOwnerToken(ownerId: String) = EventDetailsOwnerService.getOwnerToken(ownerId)

    private fun getEventParticipantList(eventId: String) =
        EventDetailsOwnerService.getEventParticipant(eventId)

    suspend fun assignOwnerToken(ownerId: String) {
        getOwnerToken(ownerId).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _ownerToken.value = state.data
                }
                is State.Failed -> {
                    val bundleTracking =
                        bundleOf("EventDetails Error [assignOwnerToken]" to state.message)
                    Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                    val message = "An error occurred [in assignUser]: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

    suspend fun assignParticipants() {
        getEventParticipantList(eventId.value!!).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _participantList.value = state.data
                }
                is State.Failed -> {
                    val bundleTracking =
                        bundleOf("EventDetails Error [assignParticipants]" to state.message)
                    Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                    val message = "An error occurred [in assignParticipants]: ${state.message}"
                    Log.i(TAG, message)
                }
            }
        }
    }

    fun acceptParticipant(participant: EventParticipant) {
        viewModelScope.launch {
            EventDetailsService.acceptParticipant(eventId.value!!, participant).collect { state ->
                when (state) {
                    is State.Loading -> {
                    }
                    is State.Success -> {
                        Log.d(TAG, "acceptParticipant Success")
                    }
                    is State.Failed -> {
                        val bundleTracking =
                            bundleOf("EventDetails Error [acceptParticipant]" to state.message)
                        Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                        Log.d(TAG, "removeParticipant Failed: ${state.message}")
                    }
                }
            }
        }
    }

    fun refuseParticipant(participant: EventParticipant) {
        viewModelScope.launch {
            EventDetailsService.refuseParticipant(eventId.value!!, participant).collect { state ->
                when (state) {
                    is State.Loading -> {
                    }
                    is State.Success -> {
                        Log.d(TAG, "refuseParticipant Success")
                    }
                    is State.Failed -> {
                        val bundleTracking =
                            bundleOf("EventDetails Error [refuseParticipant]" to state.message)
                        Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                        Log.d(TAG, "refuseParticipant Failed: ${state.message}")
                    }
                }
            }
        }
    }

    fun deleteEvent() {
        viewModelScope.launch {
            EventDetailsOwnerService.deleteEvent(eventId.value!!, club.value!!).collect { state ->
                when (state) {
                    is State.Loading -> {
                    }
                    is State.Success -> {
                        Log.d(TAG, "deleteEvent Success")
                        notifyParticipants()
                    }
                    is State.Failed -> {
                        val bundleTracking =
                            bundleOf("EventDetails Error [deleteEvent]" to state.message)
                        Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                        Log.d(TAG, "deleteEvent Failed: ${state.message}")
                    }
                }
            }
        }
    }

    private fun notifyParticipants() {
        if (!participantList.value.isNullOrEmpty() && event.value != null) {
            notifyList(event.value!!, participantList.value!!)
        }
    }

    private fun notifyList(event: Event, list: MutableList<EventParticipant>) {
        list.forEach { participant ->
            if (participant.status == "validate")
                MessagingService.eventCancelParticipation(event, participant)
        }
    }
}