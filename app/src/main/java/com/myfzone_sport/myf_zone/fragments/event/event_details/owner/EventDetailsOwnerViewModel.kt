package com.myfzone_sport.myf_zone.fragments.event.event_details.owner

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.CollectionReference
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.chat.MessagingService
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.model.event.EventParticipant
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 31/01/2021, 17:42
 *
 * Event Details Owner ViewModel class
 *
 */
class EventDetailsOwnerViewModel : ViewModel() {
    private val TAG = this::class.java.simpleName

    //region variable declaration
    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

    private val _participantList = MutableLiveData<MutableList<EventParticipant>>()
    val participantList: LiveData<MutableList<EventParticipant>>
        get() = _participantList

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

    fun acceptParticipant(participant: EventParticipant) {
        viewModelScope.launch {
            EventDetailsOwnerService.acceptParticipant(eventId.value!!, participant)
                .collect { state ->
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
            EventDetailsOwnerService.refuseParticipant(eventId.value!!, participant)
                .collect { state ->
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
            EventDetailsOwnerService.deleteEvent(eventId.value!!).collect { state ->
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