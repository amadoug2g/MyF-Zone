package com.myfzone_sport.myf_zone.app.framework

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach

/**
 * Created by Amadou on 03/09/2021, 18:30
 *
 * Firebase Services
 *
 */

object FirebaseService {
    val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val firebaseInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firebaseMsg: FirebaseMessaging by lazy { FirebaseMessaging.getInstance() }
    val TRACKING = Firebase.analytics
//    val firebaseFirestore = Firebase.firestore

    var isConnected = false
    var isAffiliated = false
    var affiliationNbr = 0

    var activeCoach: Coach? = null
    var activeCoachClubAffiliation: ClubAffiliation? = null
    var activeCoachClub: Club? = null
    var activeCoachEvents = mutableListOf<String>()
}