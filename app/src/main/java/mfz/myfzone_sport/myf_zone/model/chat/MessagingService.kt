package mfz.myfzone_sport.myf_zone.model.chat

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import mfz.myfzone_sport.myf_zone.fragments.message.MessageService
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventOwner
import mfz.myfzone_sport.myf_zone.model.event.EventParticipant
import mfz.myfzone_sport.myf_zone.util.Constants
import mfz.myfzone_sport.myf_zone.util.Notification.Companion.notificationAcceptParticipationMessage
import mfz.myfzone_sport.myf_zone.util.Notification.Companion.notificationCancelParticipationMessage
import mfz.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventAcceptParticipation
import mfz.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventCancelParticipation
import mfz.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventModifyParticipation
import mfz.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventParticipation
import mfz.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventParticipationMessage
import mfz.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventRefuseParticipation
import mfz.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventTitle
import mfz.myfzone_sport.myf_zone.util.Notification.Companion.notificationModifyParticipationMessage
import mfz.myfzone_sport.myf_zone.util.Notification.Companion.notificationRefuseParticipationMessage


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

            handleMessage(remoteMessage)
        }
    }

    companion object {
        private val TAG = MessagingService::class.java.simpleName
        private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
        private lateinit var functions: FirebaseFunctions

        fun addTokenToFireStore(newRegistrationToken: String?) {
            if (newRegistrationToken.isNullOrEmpty()) throw NullPointerException("Token is null")

            MessageService.getFCMRegistrationTokens { tokens ->
                if (tokens.contains(newRegistrationToken))
                    return@getFCMRegistrationTokens

                tokens.add(newRegistrationToken)
                MessageService.setFCMRegistrationTokens(tokens)
            }
        }

        fun eventParticipation(event: Event, coach: Coach, owner: EventOwner): Task<String> {
            functions = Firebase.functions
            val type = notificationEventParticipation()
            val title = notificationEventTitle(event)
            val message = notificationEventParticipationMessage(coach)

            val data = hashMapOf(
                "env" to Constants.ENV,
                "coachId" to owner.coachId,
                "title" to title,
                "message" to message,
                "type" to type,
                "elementId" to event.id
            )

            return functions
                .getHttpsCallable("sendNotification")
                .call(data)
                .continueWith { task ->
                    val result = task.result?.data as String
                    result
                }
        }

        fun eventAcceptParticipation(event: Event, participant: EventParticipant): Task<String> {
            functions = Firebase.functions
            val type = notificationEventAcceptParticipation()
            val title = notificationEventTitle(event)
            val message = notificationAcceptParticipationMessage()

            val data = hashMapOf(
                "env" to Constants.ENV,
                "coachId" to participant.coachId,
                "title" to title,
                "message" to message,
                "type" to type,
                "elementId" to event.id
            )

            return functions
                .getHttpsCallable("sendNotification")
                .call(data)
                .continueWith { task ->
                    val result = task.result?.data as String
                    result
                }
        }

        fun eventRefuseParticipation(event: Event, participant: EventParticipant): Task<String> {
            functions = Firebase.functions
            val type = notificationEventRefuseParticipation()
            val title = notificationEventTitle(event)
            val message = notificationRefuseParticipationMessage()

            val data = hashMapOf(
                "env" to Constants.ENV,
                "coachId" to participant.coachId,
                "title" to title,
                "message" to message,
                "type" to type,
                "elementId" to event.id
            )

            return functions
                .getHttpsCallable("sendNotification")
                .call(data)
                .continueWith { task ->
                    val result = task.result?.data as String
                    result
                }
        }

        fun eventModifyParticipation(event: Event, participant: EventParticipant): Task<String> {
            functions = Firebase.functions
            val type = notificationEventModifyParticipation()
            val title = notificationEventTitle(event)
            val message = notificationModifyParticipationMessage()

            val data = hashMapOf(
                "env" to Constants.ENV,
                "coachId" to participant.coachId,
                "title" to title,
                "message" to message,
                "type" to type,
                "elementId" to event.id
            )

            Log.i(TAG, "data? $data")

            return functions
                .getHttpsCallable("sendNotification")
                .call(data)
                .continueWith { task ->
                    val result = task.result?.data as String
                    result
                }
        }

        fun eventCancelParticipation(event: Event, participant: EventParticipant): Task<String> {
            functions = Firebase.functions
            val type = notificationEventCancelParticipation()
            val title = notificationEventTitle(event)
            val message = notificationCancelParticipationMessage()

            val data = hashMapOf(
                "env" to Constants.ENV,
                "coachId" to participant.coachId,
                "title" to title,
                "message" to message,
                "type" to type,
                "elementId" to event.id
            )

            return functions
                .getHttpsCallable("sendNotification")
                .call(data)
                .continueWith { task ->
                    val result = task.result?.data as String
                    result
                }
        }
    }

    private fun handleMessage(remoteMessage: RemoteMessage) {
        val handler = Handler(Looper.getMainLooper())

        handler.post {
            remoteMessage.notification?.let {
                val intent = Intent("MyData")
                it.body.toString()
                intent.putExtra("elementId", remoteMessage.data["elementId"])
                broadcaster?.sendBroadcast(intent)
            }
        }
    }
}