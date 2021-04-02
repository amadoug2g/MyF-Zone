package com.myfzone_sport.myf_zone.fragments.event.event_details.participant

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.model.event.EventOwner
import com.myfzone_sport.myf_zone.model.event.EventParticipant
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 29/03/2021, 16:04
 *
 * Event Details Participant ViewModel class
 *
 */

class EventDetailsParticipantViewModel : ViewModel() {
    private val TAG = this::class.java.simpleName

    //region variable declaration
    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

    private val _owner = MutableLiveData<EventOwner>()
    val owner: LiveData<EventOwner>
        get() = _owner

    private val _ownerToken = MutableLiveData<MutableList<String>>()
    val ownerToken: LiveData<MutableList<String>>
        get() = _ownerToken

    private val _participantList = MutableLiveData<MutableList<EventParticipant>>()
    val participantList: LiveData<MutableList<EventParticipant>>
        get() = _participantList

    //checks if the user is part of the initial list of participant
    private val _isUserParticipating = MutableLiveData<Boolean>(false)
    val isUserParticipating: LiveData<Boolean>
        get() = _isUserParticipating

    //checks if the user is added as a participant to the event
    private val _isUserParticipant = MutableLiveData<Boolean>(false)
    val isUserParticipant: LiveData<Boolean>
        get() = _isUserParticipant

    val eventId = MutableLiveData<String>()
    //endregion variable declaration

    fun getQuery(eventId: String): CollectionReference {
        return EventDetailsParticipantService.fireStoreInstance
            .collection(Constants.EVENT_PATH)
            .document(eventId)
            .collection("Participant")
    }

    fun checkIsUserParticipant(participant: EventParticipant): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val result = participant.coachId == currentUser?.uid
        _isUserParticipant.value = result
        return result
    }

    private fun getOwnerToken(ownerId: String) =
        EventDetailsParticipantService.getOwnerToken(ownerId)

    private fun getEventParticipantList(eventId: String) =
        EventDetailsParticipantService.getEventParticipant(eventId)

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

    fun getEvent(eventId: String) = EventDetailsParticipantService.getEvent(eventId)

    suspend fun assignEvent() {
        getEvent(eventId.value!!).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _event.value = state.data
                }
                is State.Failed -> {
                    val bundleTracking =
                        bundleOf("EventDetails Error [assignEvent]" to state.message)
                    Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                    val message = "An error occurred [in assignEvent]: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

    fun getOwnerFromEvent(eventId: String) =
        EventDetailsParticipantService.getOwnerFromEvent(eventId)

    suspend fun assignOwner() {
        getOwnerFromEvent(eventId.value!!).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _owner.value = state.data
                }
                is State.Failed -> {
                    val bundleTracking =
                        bundleOf("EventDetails Error [assignOwner]" to state.message)
                    Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                    val message = "An error occurred [in assignOwner]: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

    private fun userParticipation(eventId: String) =
        EventDetailsParticipantService.checkUserParticipation(eventId)

    suspend fun checkParticipationStatus() {
        userParticipation(eventId.value!!).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
//                    hideProgressBar()
                    _isUserParticipating.value = true
                }
                is State.Failed -> {
                    _isUserParticipating.value = false
                    val message =
                        "An error occurred [in checkParticipationStatus]: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
//                    hideProgressBar()
                }
            }
        }
    }

    fun addParticipant(participant: EventParticipant) {
        viewModelScope.launch {
            EventDetailsParticipantService.addParticipant(eventId.value!!, participant)
                .collect { state ->
                    when (state) {
                        is State.Loading -> {

                        }
                        is State.Success -> {
                            Log.d(TAG, "addParticipant Success")
                        }
                        is State.Failed -> {
                            val bundleTracking =
                                bundleOf("EventDetails Error [addParticipant]" to state.message)
                            Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                            Log.d(TAG, "addParticipant Failed: ${state.message}")
                        }
                    }
                }
        }
    }

    fun removeParticipant() {
        viewModelScope.launch {
            EventDetailsParticipantService.removeParticipant(eventId.value!!).collect { state ->
                when (state) {
                    is State.Loading -> {
                    }
                    is State.Success -> {
                        Log.d(TAG, "removeParticipant Success")
                    }
                    is State.Failed -> {
                        val bundleTracking =
                            bundleOf("EventDetails Error [removeParticipant]" to state.message)
                        Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                        Log.d(TAG, "removeParticipant Failed: ${state.message}")
                    }
                }
            }
        }
    }
}