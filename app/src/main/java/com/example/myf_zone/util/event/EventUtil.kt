package com.example.myf_zone.util.event

import android.util.Log
import com.example.myf_zone.model.event.Event
import com.example.myf_zone.model.event.EventParticipant
import com.example.myf_zone.util.Constants.DB
import com.example.myf_zone.util.Constants.EVENT_PATH
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*

object EventUtil {
    private val TAG = EventUtil::class.java.simpleName

    fun getEvents(): Task<QuerySnapshot> {
        val docRef = DB.collection(EVENT_PATH)

        return docRef.get().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "Successfully retrieved [getEvents()]")
            } else {
                Log.d(TAG, "An error occurred: ${it.exception.toString()}")
            }
        }
    }

    fun getEventList() {
        CoroutineScope(IO).launch {
            val x = getEventsByDate()
            for (i in x!!) {
                Log.d(TAG, "Inside Loop: ${i.owner.clubAcronym}")
            }

            Log.d(TAG, x.toString())
        }
    }

    val eventList: MutableList<Event> = runBlocking {
        getEventsByDate()!!
    }

    fun getEventFromId() {

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

    private suspend fun getOwnerFromEvent(eventId: String): EventParticipant? {
        val docRef =
            DB.collection(EVENT_PATH)
                .document(eventId)
                .collection("Owner")

        return try {
            docRef.get().await().documents[0].toObject<EventParticipant>()!!
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

    private fun addItem(list: MutableList<Event>, vararg item: Event) {
        for (i in item) {
            if (!list.contains(i)) {
                list.add(i)
            }
        }
    }

    private fun stampToDate(time: Timestamp): Date {
        return time.toDate()
    }
}