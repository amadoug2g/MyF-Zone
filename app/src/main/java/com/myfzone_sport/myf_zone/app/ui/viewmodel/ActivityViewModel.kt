package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.myfzone_sport.myf_zone.app.framework.FirebaseService
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.usecases.user.*
import com.myfzone_sport.myf_zone.util.Constants
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ActivityViewModel(
    private val getUserStatusUseCase: GetUserStatusUseCase
) : ViewModel() {

    var isUserConnected = MutableLiveData<Boolean>()

    init {
        getUserStatus()
    }

    fun getUserStatus(): Boolean {
        val result = getUserStatusUseCase.invoke()
        isUserConnected.postValue(result)
        return result
    }

    fun checkUserStatus() {
        try {
            if (FirebaseService.firebaseUser != null) {
                FirebaseService.isConnected = true

                val mAffiliationPath = Firebase.firestore
                    .collection(Constants.COACH_PATH + "/${FirebaseService.firebaseUser.uid}/ClubAffiliation")

                mAffiliationPath.get().addOnSuccessListener {
                    if (it.documents.size > 0) {
                        FirebaseService.isAffiliated = true
                        getActiveCoach(FirebaseService.firebaseUser)
                        getActiveClubAffiliation(FirebaseService.firebaseUser)
                    } else {
                        FirebaseService.activeCoachEvents = mutableListOf()
                        FirebaseService.isAffiliated = false
                        FirebaseService.activeCoachClubAffiliation = null
                    }
                }
            } else {
                FirebaseService.activeCoachEvents = mutableListOf()
                FirebaseService.isConnected = false
                FirebaseService.isAffiliated = false
                activeCoach = null
                FirebaseService.activeCoachClubAffiliation = null
            }
        } catch (e: Exception) {
            Log.e("ManagerAuth", "Check Status Error: ${e.localizedMessage}")
        }
    }

    private fun getActiveCoach(user: FirebaseUser?) {
        val mUserQuery = Constants.DB.document(Constants.COACH_PATH + "/${user?.uid}")

        mUserQuery.get().addOnSuccessListener {
            activeCoach = it.toObject(Coach::class.java)
        }
    }

    private fun getActiveClubAffiliation(user: FirebaseUser?) {
        val mClubQuery = Constants.DB.collection(Constants.COACH_PATH + "/${user?.uid}/ClubAffiliation")

        mClubQuery.get().addOnSuccessListener {
            val snapshot = it.documents[FirebaseService.affiliationNbr]
            FirebaseService.activeCoachClubAffiliation = snapshot.toObject(ClubAffiliation::class.java)

            getActiveClub(FirebaseService.activeCoachClubAffiliation)
            getEventsList(user, FirebaseService.activeCoachClubAffiliation)
        }
    }

    private fun getActiveClub(affiliation: ClubAffiliation?) {
        val mClubQuery = Constants.DB.document(Constants.CLUB_PATH + "/${affiliation?.clubId}")

        mClubQuery.get().addOnSuccessListener {
            FirebaseService.activeCoachClub = it.toObject(Club::class.java)
        }
    }

    private fun getEventsList(user: FirebaseUser?, affiliation: ClubAffiliation?) {
        FirebaseService.activeCoachEvents = mutableListOf()
        val mEventsQuery =
            Constants.DB.collection(Constants.COACH_PATH + "/${user?.uid}/ClubAffiliation/${affiliation?.clubId}/CoachEvent")

        mEventsQuery.get().addOnSuccessListener {
            for (doc in it) {
                FirebaseService.activeCoachEvents.add(doc.id)
            }
        }
    }
}

class ActivityViewModelFactory(
    private val getUserStatusUseCase: GetUserStatusUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetUserStatusUseCase::class.java
        )
            .newInstance(
                getUserStatusUseCase
            )
    }
}