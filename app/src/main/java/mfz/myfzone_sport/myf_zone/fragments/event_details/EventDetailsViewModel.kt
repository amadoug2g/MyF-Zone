package mfz.myfzone_sport.myf_zone.fragments.event_details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventOwner
import mfz.myfzone_sport.myf_zone.model.event.EventParticipant
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH

/**
 * Created by Amadou on 03/12/2020, 16:45
 *
 * Event Details ViewModel class
 *
 */

class EventDetailsViewModel : ViewModel() {
    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

    private val _club = MutableLiveData<ClubAffiliation>()
    val club: LiveData<ClubAffiliation>
        get() = _club

    private val _owner = MutableLiveData<EventOwner>()
    val owner: LiveData<EventOwner>
        get() = _owner

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

    private val _isUserParticipating = MutableLiveData<Boolean>(false)
    val isUserParticipating: LiveData<Boolean>
        get() = _isUserParticipating

    private val _isUserOwner = MutableLiveData<Boolean>(false)
    val isUserOwner: LiveData<Boolean>
        get() = _isUserOwner

    private val _isUserParticipant = MutableLiveData<Boolean>(false)
    val isUserParticipant: LiveData<Boolean>
        get() = _isUserParticipant

    val eventId = MutableLiveData<String>()

    init {
        _isUserSignedIn.value = checkUserSignedIn()

        viewModelScope.launch {
            assignUser()
        }
    }

    fun getQuery(eventId: String): CollectionReference {
        return EventDetailsService.fireStoreInstance
            .collection(EVENT_PATH)
            .document(eventId)
            .collection("Participant")
    }

    private fun checkUserSignedIn(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return (currentUser != null)
    }

    fun checkIsUserOwner(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val result = owner.value?.coachId == currentUser?.uid
        _isUserOwner.value = result
        return result
    }

    fun checkIsUserParticipant(participant: EventParticipant): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val result = participant.coachId == currentUser?.uid
        _isUserParticipant.value = result
        return result
    }

    fun getCurrentUser() = EventDetailsService.getCurrentUser()

    private fun getEventParticipantList(eventId: String) =
        EventDetailsService.getEventParticipant(eventId)

    private suspend fun assignUser() {
        getCurrentUser().collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _coach.value = state.data
                }
                is State.Failed -> {
//                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
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
//                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

    fun getEvent(eventId: String) = EventDetailsService.getEvent(eventId)

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
//                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

    private fun getUserClub() = EventDetailsService.getUserClub()

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
                    val message = "An error occurred: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

    fun getOwnerFromEvent(eventId: String) = EventDetailsService.getOwnerFromEvent(eventId)

    suspend fun assignOwner() {
        getOwnerFromEvent(eventId.value!!).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _owner.value = state.data
                    checkIsUserOwner()
                }
                is State.Failed -> {
//                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

    private fun affiliationStatus() = EventDetailsService.checkAffiliationStatus()

    suspend fun checkUserAffiliationStatus() {
        affiliationStatus().collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
//                    hideProgressBar()
                    _isUserAffiliated.value = true
                }
                is State.Failed -> {
                    _isUserAffiliated.value = false
                    val message = "An error occurred: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
//                    hideProgressBar()
                }
            }
        }
    }

    private fun userParticipation(eventId: String) =
        EventDetailsService.checkUserParticipation(eventId)

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
                    val message = "An error occurred: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
//                    hideProgressBar()
                }
            }
        }
    }

    fun addParticipant(eventId: String, participant: EventParticipant) =
        EventDetailsService.addParticipant(eventId, participant)

    //    fun removeParticipant(eventId: String) = EventDetailsService.removeParticipant(eventId)
    fun removeParticipant(eventId: String) {
        viewModelScope.launch {
            EventDetailsService.removeParticipant(eventId).collect { state ->
                when (state) {
                    is State.Loading -> {
                    }
                    is State.Success -> {
                        Log.d("EventDetailsViewModel", "cancellation succesful")
                    }
                    is State.Failed -> {
                        Log.d("EventDetailsViewModel", "cancellation failed: ${state.message}")
                    }
                }
            }
        }
    }

    fun acceptParticipant(eventId: String, participant: EventParticipant) =
        EventDetailsService.acceptParticipant(eventId, participant)

    fun refuseParticipant(eventId: String, participant: EventParticipant) =
        EventDetailsService.refuseParticipant(eventId, participant)

    fun deleteEvent(eventId: String, club: ClubAffiliation) =
        EventDetailsService.deleteEvent(eventId, club)
}