package mfz.myfzone_sport.myf_zone.fragments.settings

import com.google.firebase.auth.FirebaseAuth

/**
 * Created by Amadou on 29/12/2020, 14:29
 *
 * Settings Page Service
 *
 */
object SettingsService {
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun signOut() {
        firebaseAuth.signOut()
    }
}