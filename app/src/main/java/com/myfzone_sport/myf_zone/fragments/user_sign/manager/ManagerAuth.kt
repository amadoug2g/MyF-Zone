package com.myfzone_sport.myf_zone.fragments.user_sign.manager

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.myfzone_sport.myf_zone.model.club.Club
import com.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.model.coach.Coach
import com.myfzone_sport.myf_zone.util.Constants.CLUB_PATH
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB

/**
 * Created by Amadou on 13/01/2021, 14:45
 *
 * :keeps track of User Authentication Status
 *
 */

object ManagerAuth {
    private val TAG = this::class.java.simpleName

    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    var isConnected = false
    var isAffiliated = false
    var affiliationNbr = 0

    var activeCoach: Coach? = null
    var activeCoachClubAffiliation: ClubAffiliation? = null
    var activeCoachClub: Club? = null
    var activeCoachEvents = mutableListOf<String>()

    fun checkUserStatus() {
        try {
            val user: FirebaseUser? = firebaseAuth.currentUser
            if (user != null) {
                isConnected = true

                val mAffiliationPath = DB
                    .collection(COACH_PATH + "/${user.uid}/ClubAffiliation")

                mAffiliationPath.get().addOnSuccessListener {

                    if (it.documents.size > 0) {
                        isAffiliated = true
                        getActiveCoach(user)
                        getActiveClubAffiliation(user)
                        Log.i(TAG, "List: $activeCoachEvents")
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