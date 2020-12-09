package mfz.myfzone_sport.myf_zone.util.event

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.event.EventParticipant
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.getEventById

object ParticipantUtil {
    private val TAG = ParticipantUtil::class.java.simpleName

    suspend fun getParticipantsFromEvent(eventId: String): MutableList<EventParticipant>? {
        val docRef =
            DB.collection(EVENT_PATH)
                .document(eventId)
                .collection("Participant")

        return try {
            val participationList = mutableListOf<EventParticipant>()
            val documents = docRef.get().await().documents
            for (doc in documents)
                participationList.add(doc.toObject()!!)

            participationList
        } catch (e: Exception) {
            Log.e(TAG, "Error in getParticipantsFromEvent: $e")
            null
        }
    }

    //pending - validate - rejected
    fun getParticipantStatus() {}

    private suspend fun eventComplete(eventId: String): Boolean? {
        return try {
            val validParticipant = getValidParticipantCount(eventId)
            Log.d("EventStatus", "Event Participants?: $validParticipant")
            var result = false
            getEventById(eventId) { event ->
                val totalTeam = event.nbTeam
                result = if (validParticipant != null) {
                    val count = totalTeam - validParticipant
                    when (count > 0) {
                        true -> {
                            false
                        }
                        false -> {
                            true
                        }
                    }
                } else {
                    false
                }
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error in eventComplete: $e")
            null
        }
    }

    fun isEventComplete(eventId: String): Boolean? = runBlocking {
        try {
            val result = eventComplete(eventId)
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error in isEventComplete: $e")
            null
        }
    }

    suspend fun getValidParticipantCount(eventId: String): Int? {
        val participantList = getParticipantsFromEvent(eventId)
        return try {
            var result = 0
            for (item in participantList!!) {
                if (item.status == "validate")
                    result++
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error in getParticipantCount: $e")
            null
        }
    }

    suspend fun getPendingParticipantCount(eventId: String): Int? {
        return try {
            var result = 0
            for (item in getParticipantsFromEvent(eventId)!!) {
                if (item.status == "pending")
                    result++
            }

            result
        } catch (e: Exception) {
            Log.d(TAG, "Error in getParticipantCount: $e")
            null
        }
    }

    suspend fun getRefusedParticipantCount(eventId: String): Int? {
        return try {
            var result = 0
            for (item in getParticipantsFromEvent(eventId)!!) {
                if (item.status == "refused")
                    result++
            }

            result
        } catch (e: Exception) {
            Log.d(TAG, "Error in getParticipantCount: $e")
            null
        }
    }


}