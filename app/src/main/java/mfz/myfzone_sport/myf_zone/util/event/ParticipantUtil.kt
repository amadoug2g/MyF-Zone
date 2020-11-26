package mfz.myfzone_sport.myf_zone.util.event

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.event.EventParticipant
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.getEventById
import mfz.myfzone_sport.myf_zone.util.user.UserAccount
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentUser

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

    fun addParticipant(eventId: String, participant: EventParticipant) {
        val newParticipant = fieldToParticipant(participant)

        getCurrentUser {
            DB.collection(EVENT_PATH)
                .document(eventId)
                .collection("Participant").document(it.id)
                .set(newParticipant)
                .addOnSuccessListener {
                    Log.d(TAG, "Participant added successfully")
                }
                .addOnFailureListener {
                    Log.d(TAG, "Participant added failed")
                }
                .addOnCompleteListener {
                    Log.d(TAG, "Participant added completed")
                }
        }
    }

    private fun fieldToParticipant(
        participant: EventParticipant
    ): HashMap<String, Any?> {
        val result: HashMap<String, Any?> = hashMapOf(
            "clubLogo" to participant.clubLogo,
            "clubAcronym" to participant.clubAcronym,
            "coachId" to participant.coachId,
            "coachFullname" to participant.coachFullname,
            "sportId" to participant.sportId,
            "sportName" to participant.sportName,
            "status" to participant.status
        )

        if (!participant.categoryId.isNullOrEmpty() && !participant.categoryName.isNullOrEmpty()) {
            Log.d(
                TAG,
                "Participant cat\n Id is: ${participant.categoryId}.\n Name is: ${participant.categoryName}."
            )
            result["categoryId"] = participant.categoryId
            result["categoryName"] = participant.categoryName
        }

        if (!participant.subCategoryId.isNullOrEmpty() && !participant.subCategoryName.isNullOrEmpty()) {
            Log.d(
                TAG,
                "Participant sub\n Id is: ${participant.subCategoryId}\n Name is ${participant.subCategoryName}"
            )
            result["subCategoryId"] = participant.subCategoryId
            result["subCategoryName"] = participant.subCategoryName
        }

        return result
    }

    fun removeParticipant(eventId: String) {
        val currentUser = UserAccount.auth.currentUser!!

        DB.collection(EVENT_PATH)
            .document(eventId)
            .collection("Participant")
            .document(currentUser.uid)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            .addOnCompleteListener { Log.d(TAG, "Deletion completed") }
    }

    private fun removeParticipantById(eventId: String, coachId: String) {
        DB.collection(EVENT_PATH)
            .document(eventId)
            .collection("Participant")
            .document(coachId)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            .addOnCompleteListener { Log.d(TAG, "Deletion completed") }
    }

    suspend fun deleteParticipants(eventId: String) {
        try {
            val participantList =
                getParticipantsFromEvent(
                    eventId
                )!!

            if (!participantList.isNullOrEmpty()) {
                for (item in participantList) {
                    removeParticipantById(eventId, item.coachId)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in deleteParticipants", e)
        }
    }

    //pending - validate - rejected
    fun getParticipantStatus() {}

    suspend fun getParticipantCount(eventId: String): Int? {
        return try {
            getParticipantsFromEvent(eventId)?.size
        } catch (e: Exception) {
            Log.d(TAG, "Error in getParticipantCount: $e")
            null
        }
    }

    suspend fun isEventComplete(eventId: String): Boolean? {
        return try {
            val validParticipant = getValidParticipantCount(eventId)
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
            Log.w(TAG, "Error in eventComplete: $e")
            null
        }
    }

    suspend fun getValidParticipantCount(eventId: String): Int? {
        return try {
            var result = 0
            for (item in getParticipantsFromEvent(eventId)!!) {
                if (item.status == "validate")
                    result++
            }

            result
        } catch (e: Exception) {
            Log.d(TAG, "Error in getParticipantCount: $e")
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