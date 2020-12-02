package mfz.myfzone_sport.myf_zone.util.event

import android.util.Log
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventOwner
import mfz.myfzone_sport.myf_zone.model.event.EventParticipant
import mfz.myfzone_sport.myf_zone.util.Constants
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.getEventsByDate
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.auth
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getClubUser
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentUser

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

    fun addOwnerToEvent(eventId: String, owner: EventOwner) {
        val docRef = DB.collection(EVENT_PATH).document(eventId)

        docRef.collection("Owner").document(owner.coachId)
            .set(owner.toMap())
            .addOnSuccessListener {
                Log.d(TAG, "Owner added successfully")
            }
            .addOnFailureListener {
                Log.d(TAG, "Owner adding failed")
            }
            .addOnCompleteListener {
                Log.d(TAG, "Owner  adding completed")
            }
    }

    fun deleteOwner(eventId: String) {
        val currentUser = auth.currentUser

        DB.collection(EVENT_PATH)
            .document(eventId)
            .collection("Owner")
            .document(currentUser!!.uid)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            .addOnCompleteListener { Log.d(TAG, "Deletion completed") }
    }

    suspend fun deleteEventForOwner(eventId: String) {
        val currentUser = auth.currentUser
        val club = getClubUser()

        DB.collection(Constants.COACH_PATH)
            .document(currentUser!!.uid)
            .collection("ClubAffiliation")
            .document(club!!.clubId)
            .collection("CoachEvent")
            .document(eventId)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            .addOnCompleteListener { Log.d(TAG, "Deletion completed") }
    }

    fun acceptParticipant(eventId: String, participant: EventParticipant) {
        val docRef = DB.collection(EVENT_PATH).document(eventId)
        val status = setParticipantAccepted()

        docRef.collection("Participant").document(participant.coachId)
            .set(status, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Participant accepted successfully")
            }
            .addOnFailureListener {
                Log.d(TAG, "Participant accept failed")
            }
            .addOnCompleteListener {
                Log.d(TAG, "Participant accept completed")
            }
    }

    private fun setParticipantAccepted(): HashMap<String, Any?> {
        return hashMapOf(
            "status" to "validate"
        )
    }

    fun refuseParticipant(eventId: String, participant: EventParticipant) {
        val docRef = DB.collection(EVENT_PATH).document(eventId)
        val status = setParticipantRefused()

        docRef.collection("Participant").document(participant.coachId)
            .set(status, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Participant refused successfully")
            }
            .addOnFailureListener {
                Log.d(TAG, "Participant refuse failed")
            }
            .addOnCompleteListener {
                Log.d(TAG, "Participant refuse completed")
            }
    }

    private fun setParticipantRefused(): HashMap<String, Any?> {
        return hashMapOf(
            "status" to "refused"
        )
    }

    suspend fun getUserEvent(): MutableList<Event>? {
        val eventList = getEventsByDate()
        val coachEvents = mutableListOf<Event>()

        return try {
            getCurrentUser { coach ->
                if (!eventList.isNullOrEmpty()) {
                    for (event in eventList) {
                        if (event.owner.coachId == coach.id) {
                            coachEvents.add(event)
                        }
                    }
                }
            }
            coachEvents
        } catch (e: Exception) {
            Log.d(TAG, "Error: $e")
            null
        }
    }
}