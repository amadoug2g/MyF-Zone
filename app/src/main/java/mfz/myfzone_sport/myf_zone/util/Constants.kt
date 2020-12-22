package mfz.myfzone_sport.myf_zone.util

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object Constants {

    @JvmField
    val DB = Firebase.firestore

    private const val ENV_TEST = "Staging"
    private const val ENV_PROD = "Release"
    private const val ENV = ENV_TEST

    const val COACH_PATH = "Env/${ENV}/Coach"
    const val CLUB_PATH = "Env/${ENV}/Club"
    const val EVENT_PATH = "Env/${ENV}/Event"
    const val SPORT_PATH = "Env/${ENV}/Sport"

    const val BASE_URL = "https://fcm.googleapis.com"
    const val SERVER_KEY =
        "AAAA1CQea0I:APA91bGBLauSeEm3zQYhke8lecXcjo_e_1710JhoZH9ycw4VTNHAZoXPK40m0jxOtD_tNbpM_-PFf3L53OWeBJBDpVE91kXQdwFXEFN-84h9qeLVyxhYTzQz68Z7tcOrdy-mTU-rVSa_"
    const val CONTENT_TYPE = "application/json"
}