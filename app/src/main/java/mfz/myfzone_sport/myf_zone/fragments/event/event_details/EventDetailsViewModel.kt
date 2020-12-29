package mfz.myfzone_sport.myf_zone.fragments.event.event_details

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

    private fun checkIsUserOwner(): Boolean {
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

    private fun getCurrentUser() = EventDetailsService.getCurrentUser()
    private fun getOwnerToken(ownerId: String) = EventDetailsService.getOwnerToken(ownerId)

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
                    val message = "An error occurred [in assignUser]: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

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
//                    hideProgressBar()
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
//                    hideProgressBar()
                    val message = "An error occurred [in assignParticipants]: ${state.message}"
                    Log.i(TAG, message)
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
                    val message = "An error occurred [in assignEvent]: ${state.message}"
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
                    val message = "An error occurred [in assignClub]: ${state.message}"
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
                    val message = "An error occurred [in assignOwner]: ${state.message}"
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
            EventDetailsService.addParticipant(eventId.value!!, participant).collect { state ->
                when (state) {
                    is State.Loading -> {

                    }
                    is State.Success -> {
                        Log.d(TAG, "addParticipant Success")
                    }
                    is State.Failed -> {
                        Log.d(TAG, "addParticipant Failed: ${state.message}")
                    }
                }
            }
        }
    }

    fun removeParticipant() {
        viewModelScope.launch {
            EventDetailsService.removeParticipant(eventId.value!!).collect { state ->
                when (state) {
                    is State.Loading -> {
                    }
                    is State.Success -> {
                        Log.d(TAG, "removeParticipant Success")
                    }
                    is State.Failed -> {
                        Log.d(TAG, "removeParticipant Failed: ${state.message}")
                    }
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
                        Log.d(TAG, "refuseParticipant Failed: ${state.message}")
                    }
                }
            }
        }
    }

    fun deleteEvent() {
        viewModelScope.launch {
            EventDetailsService.deleteEvent(eventId.value!!, club.value!!).collect { state ->
                when (state) {
                    is State.Loading -> {
                    }
                    is State.Success -> {
                        Log.d(TAG, "deleteEvent Success")
                    }
                    is State.Failed -> {
                        Log.d(TAG, "deleteEvent Failed: ${state.message}")
                    }
                }

            }
        }
    }
}