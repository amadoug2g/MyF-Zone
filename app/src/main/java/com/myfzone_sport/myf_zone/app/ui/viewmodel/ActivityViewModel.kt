package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClub
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliation
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachEvents
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.affiliationNbr
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.firebaseUser
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isAffiliated
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isConnected
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.usecases.user.GetUserStatusUseCase
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB

//@HiltViewModel
//class ActivityViewModel @Inject constructor(
//     private val getUserStatusUseCase: GetUserStatusUseCase
//) : ViewModel() {

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
            if (firebaseUser != null) {
                isConnected = true

                val mAffiliationPath = Firebase.firestore
                    .collection(COACH_PATH + "/${firebaseUser.uid}/ClubAffiliation")

                mAffiliationPath.get().addOnSuccessListener {
                    if (it.documents.size > 0) {
                        isAffiliated = true
                        getActiveCoach(firebaseUser)
                        getActiveClubAffiliation(firebaseUser)
                    } else {
                        activeCoachEvents = mutableListOf()
                        isAffiliated = false
                        activeCoachClubAffiliation = null
                    }
                }
            } else {
                activeCoachEvents = mutableListOf()
                isConnected = false
                isAffiliated = false
                activeCoach = null
                activeCoachClubAffiliation = null
            }
        } catch (e: Exception) {
            Log.e("ManagerAuth", "Check Status Error: ${e.localizedMessage}")
        }
    }

    private fun getActiveCoach(user: FirebaseUser?) {
        val mUserQuery = DB.document(COACH_PATH + "/${user?.uid}")

        mUserQuery.get().addOnSuccessListener {
            activeCoach = it.toObject(Coach::class.java)
        }
    }

    private fun getActiveClubAffiliation(user: FirebaseUser?) {
        val mClubQuery =
            DB.collection(COACH_PATH + "/${user?.uid}/ClubAffiliation")

        mClubQuery.get().addOnSuccessListener {
            val snapshot = it.documents[affiliationNbr]
            activeCoachClubAffiliation =
                snapshot.toObject(ClubAffiliation::class.java)

            getActiveClub(activeCoachClubAffiliation)
            getEventsList(user, activeCoachClubAffiliation)
        }
    }

    private fun getActiveClub(affiliation: ClubAffiliation?) {
        val mClubQuery = DB.document(Constants.CLUB_PATH + "/${affiliation?.clubId}")

        mClubQuery.get().addOnSuccessListener {
            activeCoachClub = it.toObject(Club::class.java)
        }
    }

    private fun getEventsList(user: FirebaseUser?, affiliation: ClubAffiliation?) {
        activeCoachEvents = mutableListOf()
        val mEventsQuery =
            DB.collection(COACH_PATH + "/${user?.uid}/ClubAffiliation/${affiliation?.clubId}/CoachEvent")

        mEventsQuery.get().addOnSuccessListener {
            for (doc in it) {
                activeCoachEvents.add(doc.id)
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