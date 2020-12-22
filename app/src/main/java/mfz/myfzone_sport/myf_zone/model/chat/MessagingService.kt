package mfz.myfzone_sport.myf_zone.model.chat

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import mfz.myfzone_sport.myf_zone.fragments.message.MessageService
import org.jetbrains.anko.toast


/**
 * Created by Amadou on 20/12/2020
 */

class MessagingService : FirebaseMessagingService() {
    private val TAG = MessagingService::class.java.simpleName
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(TAG, token)
            toast("token is: $token")

            if (firebaseAuth.currentUser != null) addTokenToFireStore(token)
        })
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.notification != null)
            Log.d(TAG, "FCM message received")
    }

    companion object {
        fun addTokenToFireStore(newRegistrationToken: String?) {
            if (newRegistrationToken.isNullOrEmpty()) throw NullPointerException("Token is null")

            MessageService.getFCMRegistrationTokens { tokens ->
                if (tokens.contains(newRegistrationToken))
                    return@getFCMRegistrationTokens

                tokens.add(newRegistrationToken)
                MessageService.setFCMRegistrationTokens(tokens)
            }
        }
    }
}