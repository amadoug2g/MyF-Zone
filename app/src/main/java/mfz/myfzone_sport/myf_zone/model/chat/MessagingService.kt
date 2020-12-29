package mfz.myfzone_sport.myf_zone.model.chat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.fragments.message.MessageService
import mfz.myfzone_sport.myf_zone.screens.MainScreen


private const val CHANNEL_ID = "Notification Channel"


/**
 * Created by Amadou on 20/12/2020
 */

class MessagingService : FirebaseMessagingService() {

    private var broadcaster: LocalBroadcastManager? = null

    override fun onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this)
    }

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

            if (firebaseAuth.currentUser != null) addTokenToFireStore(token)
        })
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.notification != null) {
            Log.d(TAG, "FCM message: ${remoteMessage.data}")

            remoteMessage.notification?.let {
                Log.d(TAG, "Message Notification Body: ${it.body}")
            }

            if (remoteMessage.data["type"].equals("chatReceiveMessage")) {
                Log.d(
                    TAG,
                    "Message from ${remoteMessage.data["title"]}: ${remoteMessage.data["message"]}"
                )

//                MainScreen.binding.bottomNavBar.selectedItemId = R.id.message
            }
            handleMessage(remoteMessage)
        }
    }

    companion object {
        private val TAG = MessagingService::class.java.simpleName
        private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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

    private fun handleMessage(remoteMessage: RemoteMessage) {
        //1
        val handler = Handler(Looper.getMainLooper())

        //2
        handler.post {
            remoteMessage.notification?.let {
                val intent = Intent("MyData")
                it.body.toString()
                intent.putExtra("elementId", remoteMessage.data["elementId"])
                broadcaster?.sendBroadcast(intent)
            }

            Toast.makeText(
                baseContext, getString(R.string.notification_dot),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun navigate(destination: Int, extra: Bundle? = null) {
        findNavController(
            MainScreen::class.java.newInstance(),
            MainScreen.binding.fragmentNavHost.id
        ).navigate(destination, extra)
    }
}