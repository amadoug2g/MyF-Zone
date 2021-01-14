package mfz.myfzone_sport.myf_zone.fragments.event.event_edit

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.flow.collect
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.chat.MessagingService.Companion.eventModifyParticipation
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventParticipant
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Amadou on 02/12/2020, 21:10
 *
 * Event Edit ViewModel class
 *
 */

class EventEditViewModel : ViewModel() {
    private val TAG = this::class.java.simpleName

    private val _fields = MutableLiveData<MutableList<Place.Field>>()
    val fields: LiveData<MutableList<Place.Field>>
        get() = _fields

    private val _club = MutableLiveData<ClubAffiliation>()
    val club: LiveData<ClubAffiliation>
        get() = _club

    private val _isListInitialized = MutableLiveData<Boolean>(false)
    val isListInitialized: LiveData<Boolean>
        get() = _isListInitialized

    private val _participantList = MutableLiveData<MutableList<EventParticipant>>()
    private val participantList: LiveData<MutableList<EventParticipant>>
        get() = _participantList

    val event = MutableLiveData<Event>()

    val eventId = MutableLiveData<String>()

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

    fun initList() {
        _isListInitialized.value = true
    }

    fun getEvent(eventId: String) = EventEditService.getEvent(eventId)

    private fun getEventParticipantList(eventId: String) =
        EventEditService.getEventParticipant(eventId)

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
//                    hideProgressBar()
                    val message = "An error occurred [in assignParticipants]: ${state.message}"
                    Log.i(TAG, message)
                }
            }
        }
    }

    fun updateEvent(event: Event) = EventEditService.updateEvent(event)
    fun updateEventForOwner(event: Event, clubAffiliation: ClubAffiliation) =
        EventEditService.updateEventForOwner(event, clubAffiliation)

    private fun getUserClub() = EventEditService.getUserClub()

    suspend fun assignClub() {
        getUserClub().collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _club.value = state.data
                }
                is State.Failed -> {
//                    hideProgressBar()
                    val message = "An error occurred [in assignClub]: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

    fun getEventTypeToDisplay(event: Event): Int {
        return when (event.type) {
            "friendly" -> {
                0
            }
            "plateau" -> {
                1
            }
            "tournament" -> {
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

    fun notifyParticipants() {
        if (!participantList.value.isNullOrEmpty() && event.value != null) {
            notifyList(event.value!!, participantList.value!!)
            Log.i(TAG, "[notifyParticipants] not null")
        } else {
            Log.i(TAG, "[notifyParticipants] null?")
            Log.i(TAG, "participantList: ${participantList.value}")
            Log.i(TAG, "event: ${event.value}")
        }
    }

    private fun notifyList(event: Event, list: MutableList<EventParticipant>) {
        list.forEach { participant ->
            if (participant.status == "validate")
                eventModifyParticipation(event, participant)
        }
    }
}