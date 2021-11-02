package com.myfzone_sport.myf_zone.domain.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
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
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.fragments.message.MessageService
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.screens.MainScreen
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Notification.Companion.notificationAcceptParticipationMessage
import com.myfzone_sport.myf_zone.util.Notification.Companion.notificationCancelParticipationMessage
import com.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventAcceptParticipation
import com.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventCancelParticipation
import com.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventModifyParticipation
import com.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventParticipation
import com.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventParticipationMessage
import com.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventRefuseParticipation
import com.myfzone_sport.myf_zone.util.Notification.Companion.notificationEventTitle
import com.myfzone_sport.myf_zone.util.Notification.Companion.notificationModifyParticipationMessage
import com.myfzone_sport.myf_zone.util.Notification.Companion.notificationRefuseParticipationMessage
import com.myfzone_sport.myf_zone.util.Tracking

private const val CHANNEL_ID = "data"

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
                sendNotification(remoteMessage)
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

        fun eventParticipation(event: Event, owner: EventOwner): Task<String> {
            functions = Firebase.functions
            val type = notificationEventParticipation()
            val title = notificationEventTitle(event)
            val message = notificationEventParticipationMessage(activeCoach!!)

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

    private fun sendNotification(remoteMessage: RemoteMessage) {
        handleMessage(remoteMessage)

        when (remoteMessage.data["type"]) {
            "chatReceiveMessage" -> {

                val intent = Intent(this, MainScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                val pendingIntent = PendingIntent.getActivity(
                    this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT
                )

                val channelId = CHANNEL_ID
                val defaultSoundUri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setContentTitle(remoteMessage.notification?.title ?: "Title")
                    .setContentText(remoteMessage.notification?.body ?: "Body")
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
//                    .setColor(ContextCompat.getColor(this, R.color.colorAccent))

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Notification",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationManager.createNotificationChannel(channel)
                }


                val bundleNavigation = bundleOf("coachId" to remoteMessage.data["type"])
//                    bottomNavBar.selectedItemId = R.id.message
                try {
                    MainScreen.navController.navigate(
                        R.id.notificationCalendarToDiscussion,
                        bundleNavigation
                    )
//                        navController.navigate(R.id.discussionFragment, bundleNavigation)
                } catch (e: Exception) {
                    val bundleError =
                        bundleOf("NotificationType ${getString(R.string.error_msg)}" to e.localizedMessage)
                    Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleError)
                    Log.e(TAG, "Could not navigate to Discussion:  $e")
                }

                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
            }
            "eventModification", "eventAcceptParticipation", "eventParticipation", "eventRefuseParticipation" -> {
                val intent = Intent(this, MainScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                val pendingIntent = PendingIntent.getActivity(
                    this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT
                )

                val channelId = CHANNEL_ID
                val defaultSoundUri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setContentTitle(remoteMessage.notification?.title ?: "Title")
                    .setContentText(remoteMessage.notification?.body ?: "Body")
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
//                    .setColor(ContextCompat.getColor(this, R.color.colorAccent))

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Notification",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
            }
            else -> {

            }
        }
    }
}