package mfz.myfzone_sport.myf_zone.util.event

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventOwner
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import mfz.myfzone_sport.myf_zone.util.event.OwnerUtil.addOwnerToEvent
import mfz.myfzone_sport.myf_zone.util.event.OwnerUtil.deleteOwner
import mfz.myfzone_sport.myf_zone.util.event.OwnerUtil.getOwnerFromEvent
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.deleteParticipants
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.getParticipantsFromEvent
import mfz.myfzone_sport.myf_zone.util.user.UserAccount
import java.util.*

object EventUtil {
    private val TAG = EventUtil::class.java.simpleName

    var globalEventList: MutableList<Event>? = null
    var globalCoachEventList: MutableList<Event>? = null

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
    }

    suspend fun deleteEvent(eventId: String) {
        deleteParticipants(eventId)
        deleteOwner(eventId)

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


//    if (!categoryID.isNullOrEmpty()) {
//        categoryName = affiliationCategory
//        categoryId = categoryID
//        if (!subCategoryID.isNullOrEmpty()) {
//            subCategoryId = subCategoryID
//            subCategoryName = affiliationSubCategory
//        }
//    }


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