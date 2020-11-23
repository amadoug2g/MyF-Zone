package mfz.myfzone_sport.myf_zone.util

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object Constants {

    @JvmField
    val DB = Firebase.firestore

    const val COACH_PATH = "Env/Staging/Coach"
    const val CLUB_PATH = "Env/Staging/Club"
    const val EVENT_PATH = "Env/Staging/Event"
    const val SPORT_PATH = "Env/Staging/Sport"

}