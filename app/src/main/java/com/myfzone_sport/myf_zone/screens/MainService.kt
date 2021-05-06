package com.myfzone_sport.myf_zone.screens

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.myfzone_sport.myf_zone.model.chat.Chat
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB


/**
 * Created by Amadou on 17/12/2020
 */

object MainService {
    private val TAG = this::class.java.simpleName
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun addChatListener(
        onListen: (MutableList<Chat>) -> Unit
    ): ListenerRegistration? {
        val userId = firebaseAuth.currentUser?.uid
        val mUserChatQuery = DB
            .collection(COACH_PATH + "/${userId}/Chat")

        return try {
            mUserChatQuery
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "Error in addChatListener", error)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Chat>()
                    value?.documents?.forEach {

                        try {
                            items.add(it.toObject(Chat::class.java)!!)
                        } catch (e: Exception) {
                            Log.i(TAG, "Error when fetching chats: $e")
                        }
                    }

                    onListen(items)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in addChatListener: ${e.localizedMessage}")
            null
        }
    }


    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}