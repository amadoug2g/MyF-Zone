package com.myfzone_sport.myf_zone.fragments.event.event_details.guest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.CollectionReference
import com.myfzone_sport.myf_zone.util.Constants

/**
 * Created by Amadou on 31/01/2021, 16:40
 *
 * Event Details Guest ViewModel class
 *
 */

class EventDetailsGuestViewModel : ViewModel() {
    private val TAG = this::class.java.simpleName

    //region variable declaration
    val eventId = MutableLiveData<String>()
    //endregion variable declaration

    fun getQuery(eventId: String): CollectionReference {
        return EventDetailsGuestService.fireStoreInstance
            .collection(Constants.EVENT_PATH)
            .document(eventId)
            .collection("Participant")
    }

    fun getEvent(eventId: String) = EventDetailsGuestService.getEvent(eventId)

    fun getOwnerFromEvent(eventId: String) = EventDetailsGuestService.getOwnerFromEvent(eventId)
}