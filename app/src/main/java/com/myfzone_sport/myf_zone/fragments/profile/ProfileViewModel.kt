package com.myfzone_sport.myf_zone.fragments.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.CollectionReference
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.util.Constants

/**
 * Created by Amadou on 01/12/2020, 23:47
 *
 * Profile ViewModel class
 *
 */

class ProfileViewModel : ViewModel() {

    fun getQuery(): CollectionReference {
        return ProfileService.fireStoreInstance
            .collection(Constants.COACH_PATH)
            .document(ManagerAuth.activeCoach!!.id)
            .collection("ClubAffiliation")
            .document(ManagerAuth.activeCoachClubAffiliation!!.clubId)
            .collection("CoachEvent")
    }

    fun getCurrentUserEvents() = ProfileService.getUserEventList()
}