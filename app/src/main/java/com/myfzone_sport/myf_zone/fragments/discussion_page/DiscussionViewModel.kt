package com.myfzone_sport.myf_zone.fragments.discussion_page

import android.content.Context
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Tracking
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.coroutines.flow.collect

/**
 * Created by Amadou on 07/12/2020, 12:03
 *
 * Discussion ViewModel class
 *
 */

class DiscussionViewModel : ViewModel() {
    private val TAG = this::class.java.simpleName
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

    val isUserTyping = MutableLiveData<Boolean>(false)

    var textMessage = MutableLiveData<String>()

    private fun getClub(userId: String) = DiscussionService.getUserClub(userId)

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
                    val bundleTracking = bundleOf("Discussion Error" to state.message)
                    Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

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
                    val bundleTracking = bundleOf("Discussion Error" to state.message)
                    Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                    val message = "An error occurred: ${state.message}"
                    Log.i("DiscussionVM [Club 2]", message)
                }
            }
        }
    }

    fun getOrCreateChat(
        other: Coach,
        otherClub: ClubAffiliation,
        message: String,
        photo: String
    ) = DiscussionService.getOrCreateChat(other, otherClub, message, photo)

    fun addChatMessageListener(
        otherId: String,
        context: Context,
        onListen: (List<Item>) -> Unit
    ) = DiscussionService.addChatMessageListener(otherId, context, onListen)

    fun setDiscussionRead(
        other: Coach
    ) = DiscussionService.setDiscussionRead(other)

    fun discussionHasMessages(
        other: Coach
    ) = DiscussionService.discussionHasMessages(other)

    fun setUserTyping(
        other: Coach
    ) = DiscussionService.setUserTyping(other, isUserTyping.value!!)

    fun typeStart() {
        isUserTyping.value = true
        Log.i(TAG, "[typeStart] isUserTyping: ${isUserTyping.value}")
    }

    fun typeStop() {
        isUserTyping.value = false
        Log.i(TAG, "[typeStop] isUserTyping: ${isUserTyping.value}")
    }
}