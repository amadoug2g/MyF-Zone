package mfz.myfzone_sport.myf_zone.util.event

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.event.EventOwner
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH

object OwnerUtil {
    private val TAG = OwnerUtil::class.java.simpleName

    suspend fun getOwnerFromEvent(eventId: String): EventOwner? {
        val docRef =
            DB.collection(EVENT_PATH)
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