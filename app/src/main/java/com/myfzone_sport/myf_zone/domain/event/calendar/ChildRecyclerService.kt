package com.myfzone_sport.myf_zone.domain.event.calendar

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.util.Constants
import kotlinx.coroutines.tasks.await

/**
 * Created by Amadou on 08/04/2021, 22:04
 *
 * ChildRecycler Page Service
 *
 */

class ChildRecyclerService {
    private val TAG = this::class.java.simpleName

    suspend fun getOwnerFromEvent(eventId: String): EventOwner? {
        val docRef =
            Constants.DB.collection(Constants.EVENT_PATH)
                .document(eventId)
                .collection("Owner")

        return try {
            docRef.get().await().documents[0].toObject<EventOwner>()!!
        } catch (e: Exception) {
            Log.e(TAG, "Error in getOwnerFromEvent: $e")
            null
        }
    }
}