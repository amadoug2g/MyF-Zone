package com.myfzone_sport.myf_zone.fragments.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.myfzone_sport.myf_zone.model.coach.Coach
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH

/**
 * Created by Amadou on 07/12/2020, 01:03
 *
 * Message ViewModel class
 *
 */

class MessageViewModel : ViewModel() {

    private val _coach = MutableLiveData<Coach>()
    val coach: LiveData<Coach>
        get() = _coach

    private val _query = MutableLiveData<CollectionReference>()
    val query: LiveData<CollectionReference>
        get() = _query

    init {
        _query.value = getQuery()
    }

    private fun getQuery(): CollectionReference {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return MessageService.fireStoreInstance
            .collection(COACH_PATH + "/${currentUser?.uid}/Chat")
    }

    private fun getChatCoach(coachId: String) = MessageService.getChatCoach(coachId)
}