package mfz.myfzone_sport.myf_zone.fragments.event.event_creation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventOwner
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Amadou on 03/12/2020, 16:49
 *
 * Event Creation ViewModel class
 *
 */

class EventCreationViewModel : ViewModel() {
    private val _fields = MutableLiveData<MutableList<Place.Field>>()
    val fields: LiveData<MutableList<Place.Field>>
        get() = _fields

    private fun initFields() {
        _fields.value = mutableListOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
    }

    init {
        initFields()
    }

    fun checkAffiliationStatus() =
        EventCreationService.checkAffiliationStatus()

    fun getOwnerForEvent() =
        EventCreationService.getOwnerForEvent()

    fun createEvent(event: Event) =
        EventCreationService.createEvent(
            event
        )

    fun addOwnerToEvent(event: Event, owner: EventOwner) =
        EventCreationService.addOwnerToEvent(
            event,
            owner
        )

    fun addEventToUser(event: Event, owner: EventOwner, club: ClubAffiliation) =
        EventCreationService.addEventToUser(
            event,
            owner,
            club
        )

    fun setEventType(type: String, event: Event) {
        when (type) {
            "Match Amical" -> {
                event.type = "friendly"
            }
            "Plateau" -> {
                event.type = "plateau"
            }
            "Tournoi" -> {
                event.type = "tournament"
            }
        }
    }

    fun setEventTeam(nbTeam: Int, event: Event) {
        event.nbTeam = nbTeam
    }

    fun setEventDate(eventDay1: String, eventDay2: String, eventTime: String, event: Event): Date? {
        val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val date = formatDate.parse("$eventDay1 $eventTime $eventDay2")
        event.createdDate = Calendar.getInstance().time
        try {
            event.date = date!!
        } catch (e: Exception) {
            Log.d("EventEditViewModel", "An error occurred in setEventDate: ${e.localizedMessage}")
        }
        return date
    }
}