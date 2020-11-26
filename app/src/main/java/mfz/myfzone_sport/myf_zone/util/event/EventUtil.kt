package mfz.myfzone_sport.myf_zone.util.event

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventOwner
import mfz.myfzone_sport.myf_zone.model.event.MarkerItem
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import mfz.myfzone_sport.myf_zone.util.event.OwnerUtil.addOwnerToEvent
import mfz.myfzone_sport.myf_zone.util.event.OwnerUtil.deleteEventForOwner
import mfz.myfzone_sport.myf_zone.util.event.OwnerUtil.deleteOwner
import mfz.myfzone_sport.myf_zone.util.event.OwnerUtil.getOwnerFromEvent
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.deleteParticipants
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.getParticipantsFromEvent
import mfz.myfzone_sport.myf_zone.util.user.UserAccount
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentClub
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentUser
import java.util.*

object EventUtil {
    private val TAG = EventUtil::class.java.simpleName

    var globalEventList: MutableList<Event>? = null
    var globalCoachEventList: MutableList<Event>? = null
    var markerItemList: MutableList<MarkerItem>? = mutableListOf()

    fun getEventById(eventId: String, onComplete: (Event) -> Unit) {
        DB.collection(EVENT_PATH)
            .document(eventId).get()
            .addOnSuccessListener {
                it.toObject<Event>()?.let { it1 -> onComplete(it1) }
            }
    }

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

    fun createEvent(event: Event, owner: EventOwner) {
        val docRef = DB.collection(EVENT_PATH).document()
        event.id = docRef.id

        val newEvent = fieldToEvent(event)

        DB.collection(EVENT_PATH).document(event.id).set(newEvent)
            .addOnSuccessListener {
                Log.d(TAG, "Event created successfully")
            }
            .addOnFailureListener {
                Log.d(TAG, "Event creation failed")
            }
            .addOnCompleteListener {
                Log.d(TAG, "Event creation completed")
            }

        addOwnerToEvent(event.id, owner)
        addEventToUser(newEvent, event.id)
    }

    private fun addEventToUser(event: HashMap<String, Any?>, id: String) {
        getCurrentUser { coach ->
            getCurrentClub { club ->
                DB.collection(COACH_PATH)
                    .document(coach.id)
                    .collection("ClubAffiliation")
                    .document(club.clubId)
                    .collection("CoachEvent")
                    .document(id).set(event)
                    .addOnSuccessListener {
                        Log.d(TAG, "Event added to user successfully")
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "Event adding to user failed")
                    }
                    .addOnCompleteListener {
                        Log.d(TAG, "Event adding to user completed")
                    }
            }
        }
    }

    suspend fun getEventsByUser(coachId: String, clubId: String): MutableList<Event>? {
        return try {
            val eventList = mutableListOf<Event>()

            val docRef =
                DB.collection(COACH_PATH)
                    .document(coachId)
                    .collection("ClubAffiliation")
                    .document(clubId)
                    .collection("CoachEvent")

            val documents = docRef.get().await().documents
            for (doc in documents) {
                eventList.add(doc.toObject()!!)
            }

            eventList
        } catch (e: Exception) {
            Log.d(TAG, "Error: $e")
            null
        }

    }

    suspend fun deleteEvent(eventId: String) {
        deleteParticipants(eventId)
        deleteOwner(eventId)
        deleteEventForOwner(eventId)

        DB.collection(EVENT_PATH)
            .document(eventId)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            .addOnCompleteListener { Log.d(TAG, "Deletion completed") }
    }

    private fun fieldToUpdatedEvent(event: Event): HashMap<String, Any?> {
        return hashMapOf(
            "title" to event.title,
            "description" to event.description,
            "type" to event.type,
            "nbTeam" to event.nbTeam,
            "date" to event.date,
            "address" to event.address,
            "lat" to event.lat,
            "lng" to event.lng
        )
    }

    private fun fieldToEvent(event: Event): HashMap<String, Any?> {
        return hashMapOf(
            "id" to event.id,
            "title" to event.title,
            "description" to event.description,
            "type" to event.type,
            "nbTeam" to event.nbTeam,
            "date" to event.date,
            "address" to event.address,
            "lat" to event.lat,
            "lng" to event.lng,
            "createdDate" to event.createdDate
        )
    }

    fun updateEvent(eventId: String, event: Event) {
        val newEvent = fieldToUpdatedEvent(event)

        Log.d(TAG, "Event id is ${event.id}")

        DB.collection(EVENT_PATH)
            .document(eventId)
            .set(newEvent, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Event updated successfully")
            }
            .addOnFailureListener {
                Log.d(TAG, "Event update failed")
            }
            .addOnCompleteListener {
                Log.d(TAG, "Event update complete")
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