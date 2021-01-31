package com.myfzone_sport.myf_zone.fragments.user_sign.manager

import com.google.firebase.auth.FirebaseAuth
import com.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.model.coach.Coach
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB

/**
 * Created by Amadou on 13/01/2021, 14:45
 *
 * : keeps track of User Authentication Status
 *
 */

object ManagerAuth {
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    var isConnected = false
    var isAffiliated = false

    var user = firebaseAuth.currentUser
    var activeCoach: Coach? = null
    var activeCoachClub: ClubAffiliation? = null

    fun checkUserStatus() {
        if (user != null) {
            isConnected = true

            val mAffiliationPath = DB
                .collection(COACH_PATH + "/${user?.uid}/ClubAffiliation")

            mAffiliationPath.get().addOnSuccessListener {

                if (it.documents.size > 0) {
                    isAffiliated = true
                    getActiveClub()
                    getActiveCoach()
                }
            }
        }
    }

    private fun getActiveCoach() {
        val mUserQuery = DB.document(COACH_PATH + "/${user?.uid}")

        mUserQuery.get().addOnSuccessListener {
            activeCoach = it.toObject(Coach::class.java)
        }
    }

    private fun getActiveClub() {
        val mClubQuery = DB.document(COACH_PATH + "/${user?.uid}/ClubAffiliation")

        mClubQuery.get().addOnSuccessListener {
            activeCoachClub = it.toObject(ClubAffiliation::class.java)
        }
    }


}