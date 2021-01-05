package mfz.myfzone_sport.myf_zone.util

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object Constants {

    @JvmField
    val DB = Firebase.firestore
    val TRACKING = Firebase.analytics

    private const val ENV_TEST = "Staging"
    private const val ENV_PROD = "Release"

    const val ENV = ENV_TEST
    const val COACH_PATH = "Env/${ENV}/Coach"
    const val CLUB_PATH = "Env/${ENV}/Club"
    const val EVENT_PATH = "Env/${ENV}/Event"
    const val SPORT_PATH = "Env/${ENV}/Sport"
}