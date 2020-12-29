package mfz.myfzone_sport.myf_zone.fragments.message

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
import mfz.myfzone_sport.myf_zone.model.chat.Chat
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH

/**
 * Created by Amadou on 07/12/2020, 01:03
 *
 * Message ViewModel class
 *
 */

class MessageViewModel : ViewModel() {

    private val _isUserSignedIn = MutableLiveData<Boolean>(false)
    val isUserSignedIn: LiveData<Boolean>
        get() = _isUserSignedIn

    private val _isUserAffiliated = MutableLiveData<Boolean>(false)
    val isUserAffiliated: LiveData<Boolean>
        get() = _isUserAffiliated

    private val _isConversationRead = MutableLiveData<Boolean>(false)
    val isConversationRead: LiveData<Boolean>
        get() = _isConversationRead

    private val _newChat = MutableLiveData<Boolean>(false)
    val newChat: LiveData<Boolean>
        get() = _newChat

    private val _coach = MutableLiveData<Coach>()
    val coach: LiveData<Coach>
        get() = _coach

    private val _coachChat = MutableLiveData<Chat>()
    val coachChat: LiveData<Chat>
        get() = _coachChat

    private val _query = MutableLiveData<CollectionReference>()
    val query: LiveData<CollectionReference>
        get() = _query

    init {
        _isUserSignedIn.value = checkUserSignedIn()
        _query.value = getQuery()

        viewModelScope.launch {
            assignUser()
        }
    }

    private fun getQuery(): CollectionReference {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return MessageService.fireStoreInstance
            .collection(COACH_PATH + "/${currentUser?.uid}/Chat")
    }

    fun userHasMessages(value: Boolean) {
        _newChat.value = value
    }

    fun checkUserSignedIn(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return (currentUser != null)
    }

    fun getCurrentUser() = MessageService.getCurrentUser()

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

    private fun getChatCoach(coachId: String) = MessageService.getChatCoach(coachId)

    suspend fun assignCoachChat(coachId: String) {
        getChatCoach(coachId).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _coachChat.value = state.data
                }
                is State.Failed -> {
//                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }

    private fun affiliationStatus() = MessageService.checkAffiliationStatus()

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
                    val message = "An error occurred: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
//                    hideProgressBar()
                }
            }
        }
    }
}