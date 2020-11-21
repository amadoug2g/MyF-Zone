package com.example.myf_zone.util.event

import android.util.Log
import com.example.myf_zone.model.event.Event
import com.example.myf_zone.model.event.EventOwner
import com.example.myf_zone.model.event.EventParticipant
import com.example.myf_zone.util.Constants.DB
import com.example.myf_zone.util.Constants.EVENT_PATH
import com.example.myf_zone.util.user.UserAccount
import com.example.myf_zone.util.user.UserAccount.getCurrentUser
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import java.util.*

object EventUtil {
    private val TAG = EventUtil::class.java.simpleName

    private val currentUser = UserAccount.auth.currentUser

//    var globalEventList = mutableListOf<Event>()

    fun getEventFromId(eventId: String, onComplete: (Event) -> Unit) {
        DB.collection(EVENT_PATH)
            .document(eventId).get()
            .addOnSuccessListener {
                it.toObject<Event>()?.let { it1 -> onComplete(it1) }
            }
    }

    fun getEventById(eventId: String, onComplete: (Event) -> Unit) {
        DB.collection(EVENT_PATH)
            .document(eventId).get()
            .addOnSuccessListener {
                it.toObject<Event>()?.let { it1 -> onComplete(it1) }
//                Log.d(TAG, "Event retrieved")
            }
    }

//    val globalEventList: MutableList<Event> = runBlocking {
//        getEventsByDate()!!
//    }

    suspend fun getEventsByDate(): MutableList<Event>? {
        val docRef = DB.collection(EVENT_PATH).orderBy("date")

        return try {
            val eventList = mutableListOf<Event>()
            val documents = docRef.get().await().documents
            for (doc in documents) {
                val owner =
                    getOwnerFromEvent(
                        doc.id
                    )
                val participantList =
                    getParticipantsFromEvent(
                        doc.id
                    )
                val eventToAdd: Event = doc.toObject()!!

                eventToAdd.owner = owner!!
                eventToAdd.participants = participantList!!

                eventList.add(eventToAdd)
            }

            eventList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    suspend fun getOwnerFromEvent(eventId: String): EventOwner? {
        val docRef =
            DB.collection(EVENT_PATH)
                .document(eventId)
                .collection("Owner")

        return try {
            docRef.get().await().documents[0].toObject<EventOwner>()!!
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private suspend fun getParticipantsFromEvent(eventId: String): MutableList<EventParticipant>? {
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
            Log.e(TAG, e.toString())
            null
        }
    }

    fun addParticipant(eventId: String, participant: EventParticipant) {
        getCurrentUser {
            DB.collection(EVENT_PATH)
                .document(eventId)
                .collection("Participant").document(it.id)
                .set(participant)
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

    fun removeParticipant(eventId: String) {
        val currentUser = UserAccount.auth.currentUser!!

        val path = DB.collection(EVENT_PATH)
            .document(eventId)
            .collection("Participant")
            .document(currentUser.uid)

        Log.d(TAG, "Path is: ${path.path}")

        DB.collection(EVENT_PATH)
            .document(eventId)
            .collection("Participant")
            .document(currentUser.uid)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            .addOnCompleteListener { Log.d(TAG, "Deletion completed") }
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

//    suspend fun eventComplete(eventId: String): Boolean{
//        val place: Int = getEventFromId(eventId) {event ->
//            val accepted = getValidParticipantCount(eventId)!!
//            if (accepted == event.nbTeam)
//
//        }
//    }

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

    fun createEvent(event: Event) {
        DB.collection(EVENT_PATH)
            .add(event)
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

    fun updateEvent(event: Event) {
        DB.collection(EVENT_PATH)
            .document(event.id)
            .set(event)
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

    suspend fun checkUserParticipation(eventId: String): Boolean? {
        val participantList =
            getParticipantsFromEvent(
                eventId
            )!!

        val currentUser = UserAccount.auth.currentUser!!

        return try {
            val idList = mutableListOf<String>()
            for (item in participantList)
                idList.add(item.coachId)

            Log.d(TAG, idList.toString())
            Log.d(TAG, "User ID is: ${currentUser.uid}")
            idList.contains(currentUser.uid)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private fun stampToDate(time: Timestamp): Date {
        return time.toDate()
    }
}