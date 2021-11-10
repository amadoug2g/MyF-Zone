package com.myfzone_sport.myf_zone.app.framework

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.util.Constants.CLUB_PATH
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB

/**
 * Created by Amadou on 03/09/2021, 18:30
 *
 * Firebase Services
 *
 */

object FirebaseService {
    private val TAG = this::class.java.simpleName
    val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val firebaseInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firebaseMsg: FirebaseMessaging by lazy { FirebaseMessaging.getInstance() }
    val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
    val TRACKING = Firebase.analytics
//    val firebaseFirestore = Firebase.firestore

    var isConnected = false
    var isAffiliated = false
    var affiliationNbr = 0

    var activeCoach: Coach? = null
    var activeCoachClubAffiliation: ClubAffiliation? = null
    var activeCoachClub: Club? = null
    var activeCoachEvents = mutableListOf<String>()

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
            Log.e("ManagerAuth", "$TAG Status Error: ${e.localizedMessage}")
        }
    }

    private fun getActiveCoach(user: FirebaseUser?) {
        val mUserQuery = DB.document(COACH_PATH + "/${user?.uid}")

        mUserQuery.get().addOnSuccessListener {
            activeCoach = it.toObject(Coach::class.java)
        }
    }

    private fun getActiveClubAffiliation(user: FirebaseUser?) {
        val mClubQuery = DB.collection(COACH_PATH + "/${user?.uid}/ClubAffiliation")

        mClubQuery.get().addOnSuccessListener {
            val snapshot = it.documents[affiliationNbr]
            activeCoachClubAffiliation = snapshot.toObject(ClubAffiliation::class.java)

            getActiveClub(activeCoachClubAffiliation)
            getEventsList(user, activeCoachClubAffiliation)
        }
    }

    private fun getActiveClub(affiliation: ClubAffiliation?) {
        val mClubQuery = DB.document(CLUB_PATH + "/${affiliation?.clubId}")

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

    fun isCoachOwner(eventId: String): Boolean {
        return (activeCoachEvents.contains(eventId))
    }
}