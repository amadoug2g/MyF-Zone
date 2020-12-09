package mfz.myfzone_sport.myf_zone.fragments.event.event_edit

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place
import mfz.myfzone_sport.myf_zone.model.event.Event
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Amadou on 02/12/2020, 21:10
 *
 * Event Edit ViewModel class
 *
 */

class EventEditViewModel : ViewModel() {
    private val _fields = MutableLiveData<MutableList<Place.Field>>()
    val fields: LiveData<MutableList<Place.Field>>
        get() = _fields

    val event = MutableLiveData<Event>()

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

    fun getEvent(eventId: String) = EventEditService.getEvent(eventId)

    fun updateEvent(eventId: String, event: Event) = EventEditService.updateEvent(eventId, event)

    fun getEventTypeToDisplay(event: Event): Int {
        return when (event.type) {
            "Match Amical" -> {
                0
            }
            "Plateau" -> {
                1
            }
            "Tournoi" -> {
                2
            }
            else -> {
                -1
            }
        }
    }

    fun getEventNbTeamToDisplay(event: Event): Int {
        return (event.nbTeam - 2)
    }

    fun getEventDay(event: Event): String {
        val date = DateFormat.format("dd/MM/yyyy", event.date)

        return date.toString()
    }

    fun getEventHour(event: Event): String {
        val time = DateFormat.format("HH:mm", event.date)

        return time.toString()
    }

    fun setEventType(type: String) {
        when (type) {
            "Match Amical" -> {
                event.value?.type = "friendly"
            }
            "Plateau" -> {
                event.value?.type = "plateau"
            }
            "Tournoi" -> {
                event.value?.type = "tournament"
            }
        }
    }

    fun setEventTeam(nbTeam: Int) {
        event.value?.nbTeam = nbTeam
    }

    fun setEventDate(eventDay1: String, eventDay2: String, eventTime: String): Date? {
        val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        val date = formatDate.parse("$eventDay1 $eventTime $eventDay2")
        try {
            event.value?.date = date!!
        } catch (e: Exception) {
            Log.d("EventEditViewModel", "An error occurred in setEventDate: ${e.localizedMessage}")
        }
        return date
    }
}