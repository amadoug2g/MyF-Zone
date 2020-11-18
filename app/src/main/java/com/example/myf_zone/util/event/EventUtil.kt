package com.example.myf_zone.util.event

import android.util.Log
import com.example.myf_zone.model.event.Event
import com.example.myf_zone.model.event.EventOwner
import com.example.myf_zone.model.event.EventParticipant
import com.example.myf_zone.util.Constants.DB
import com.example.myf_zone.util.Constants.EVENT_PATH
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import java.util.*

object EventUtil {
    private val TAG = EventUtil::class.java.simpleName

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
//                Log.d(TAG, "Club retrieved")
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
//                Log.d(TAG,"Event list: $eventToAdd")
            }

//            Log.d(TAG,"Event list: $eventList")
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
                .collection("Participants")

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

    private fun stampToDate(time: Timestamp): Date {
        return time.toDate()
    }
}