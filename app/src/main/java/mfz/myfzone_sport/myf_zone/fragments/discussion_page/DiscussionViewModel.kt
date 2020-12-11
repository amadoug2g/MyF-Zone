package mfz.myfzone_sport.myf_zone.fragments.discussion_page

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.util.user.UserAccount

/**
 * Created by Amadou on 07/12/2020, 12:03
 *
 * Discussion ViewModel class
 *
 */

class DiscussionViewModel : ViewModel() {
    private val _coach = MutableLiveData<Coach>()
    val coach: LiveData<Coach>
        get() = _coach

    private val _coachClub = MutableLiveData<ClubAffiliation>()
    val coachClub: LiveData<ClubAffiliation>
        get() = _coachClub

    private val _other = MutableLiveData<Coach>()
    val other: LiveData<Coach>
        get() = _other

    private val _otherClub = MutableLiveData<ClubAffiliation>()
    val otherClub: LiveData<ClubAffiliation>
        get() = _otherClub

    val isTextEmpty = MutableLiveData<Boolean>()

    val isUserTyping = MutableLiveData<Boolean>()

    var textMessage = MutableLiveData<String>()

    init {
        viewModelScope.launch {
//            assignCurrentUser()
//            assignCurrentClub()
        }
    }

    private fun getCurrentUser() = DiscussionService.getCurrentUser()

    suspend fun assignCurrentUser() {
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
                    Log.i("DiscussionVM [User]", message)
                }
            }
        }
    }

    private fun getClub(userId: String) = DiscussionService.getUserClub(userId)

    suspend fun assignCurrentClub() {
        val currentUser = UserAccount.auth.currentUser
        getClub(currentUser!!.uid).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _coachClub.value = state.data
                }
                is State.Failed -> {
//                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    Log.i("DiscussionVM [UserClub]", message)
                }
            }
        }
    }

    private fun getDiscussionUser(coachId: String) = DiscussionService.getDiscussionUser(coachId)

    suspend fun assignDiscussionUser(coachId: String) {
        getDiscussionUser(coachId).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _other.value = state.data
                }
                is State.Failed -> {
//                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    Log.i("DiscussionVM [User 2]", message)
                }
            }
        }
    }

    suspend fun assignOtherClub(userId: String) {
        getClub(userId).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _otherClub.value = state.data
                }
                is State.Failed -> {
//                    hideProgressBar()
                    val message = "An error occurred: ${state.message}"
                    Log.i("DiscussionVM [Club 2]", message)
                }
            }
        }
    }

    fun typeStart() {
        isUserTyping.value = true
    }

    fun typeStop() {
        isUserTyping.value = false
    }
}