package com.myfzone_sport.myf_zone.fragments.message

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.chat.Chat
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

/**
 * Created by Amadou on 07/12/2020, 01:04
 *
 * Message Page Service
 *
 */

object MessageService {
    private val TAG = this::class.java.simpleName
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = fireStoreInstance
            .document(
                COACH_PATH + "/${
                FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception(
                    "You are not connected"
                )
                }"
            )

    fun getChatCoach(coachId: String) = flow<State<Chat>> {
        val userId = firebaseAuth.currentUser?.uid
        val mUserQuery = DB
            .document(COACH_PATH + "${userId}/Chat/${coachId}")

        emit(State.loading())

        val snapshot = mUserQuery.get().await()
        val coachChat = snapshot.toObject(Chat::class.java)

        emit(State.success(coachChat!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    //region FCM
    fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject(Coach::class.java)
            try {
                onComplete(user!!.devices)
            } catch (e: Exception) {
                Log.e(TAG, "Error in [getFCMRegistrationTokens]: ${e.localizedMessage}")
            }
        }
    }

    fun setFCMRegistrationTokens(registrationTokens: MutableList<String>) {
        currentUserDocRef.update(mapOf("devices" to registrationTokens))
    }
    //endregion FCM

    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}