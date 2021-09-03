package com.myfzone_sport.myf_zone.fragments.calendar

import android.text.format.DateFormat
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventCalendar
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.domain.event.calendar.EventSection
import com.myfzone_sport.myf_zone.util.Constants.DB
import com.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Amadou on 03/12/2020, 16:49
 *
 * Calendar Page Service
 *
 */

object CalendarService {

    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val TAG = CalendarService::class.java.simpleName
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun getEvents() = flow<State<MutableList<Event>>> {
        emit(State.loading())
        val now = Calendar.getInstance().time
        FirebaseFirestore.getInstance()

        //val settings : FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder().apply {
        //    isPersistenceEnabled = true
        //}.build()

        //FirebaseFirestore.getInstance().firestoreSettings = settings
        val mEventQuery = DB.collection(EVENT_PATH).orderBy("date")

        val snapshot = mEventQuery.get().await()
        val eventList: MutableList<Event> = mutableListOf()

        snapshot.forEach {
            val event = it.toObject<Event>()
            if (event.date.time > now.time) eventList.add(event)
        }

        eventList.forEach { event ->
            val owner = getOwnerFromEvent(event.id)
            val participantList = getParticipantsFromEvent(event.id)

            event.owner = owner!!
            event.participants = participantList!!
        }

        emit(State.success(eventList))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    private suspend fun getOwnerFromEvent(eventId: String): EventOwner? {
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
            Log.e(TAG, "Error in getParticipantsFromEvent: $e")
            null
        }
    }

    private fun sortedEventList(eventList: MutableList<Event>): MutableList<HashMap<String, MutableList<EventCalendar>>> {
        val result: MutableList<Event> = eventList
        val tempList: MutableList<EventCalendar> = mutableListOf()

        val formatEventDay = SimpleDateFormat("d", Locale.FRANCE)
        val formatEventMonth = SimpleDateFormat("MMMM", Locale.FRANCE)
        val formatEventYear = SimpleDateFormat("yyyy", Locale.FRANCE)
        val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

        val currentYear = DateFormat.format("yyyy", Date()).toString()

        result.sortBy { it.date }

        for (event in result) {

            val eventDay = formatEventDay.format(formatDate.parse(event.date.toString())!!)
            val eventMonth =
                formatEventMonth.format(formatDate.parse(event.date.toString())!!).capitalize()
//            eventMonth.capitalize(Locale.getDefault())
//            val eventMonth = formatEventMonth.format(formatDate.parse(event.date.toString())!!).toUpperCase(Locale.getDefault())
            val eventYear = formatEventYear.format(formatDate.parse(event.date.toString())!!)

            val eventDate =
                if (eventYear == currentYear) "$eventDay $eventMonth" else "$eventDay $eventMonth $eventYear"

            val calendar = EventCalendar().apply {
                id = event.id
                title = event.title
                description = event.description
                type = event.type
                nbTeam = event.nbTeam
                date = event.date
                month = eventDate
                address = event.address
                lat = event.lat
                lng = event.lng
                createdDate = event.createdDate
                owner = event.owner
                participants = event.participants
            }

//            if (calendar.date > Calendar.getInstance().time)
            tempList.add(calendar)
        }

        val setMonth = mutableSetOf<String>()

        for (i in tempList)
            setMonth.add(i.month)

        val list = mutableListOf<HashMap<String, MutableList<EventCalendar>>>()

        for (month in setMonth) {
            val calendarList = mutableListOf<EventCalendar>()
            val map = hashMapOf<String, MutableList<EventCalendar>>()
            for (event in tempList) {
                if (month == event.month) {
                    calendarList.add(event)
                }
            }
            map[month] = calendarList
            list.add(map)
        }

        return list
    }

    private fun mapToEventSection(calendarList: MutableList<HashMap<String, MutableList<EventCalendar>>>): MutableList<EventSection> {
        val resultList = mutableListOf<EventSection>()

        for (item in calendarList) {
            for (j in item.keys) {
                val section = EventSection(j, item[j]!!)
                resultList.add(section)
            }
        }

        return resultList
    }

    fun eventToCalendar(eventList: MutableList<Event>): MutableList<EventSection> {
        val list = sortedEventList(eventList)
        return mapToEventSection(list)
    }

    fun addEventListener(
        onListen: (MutableList<Event>) -> Unit
    ): ListenerRegistration? {
        val now = Calendar.getInstance().time

        val mUserChatQuery = DB
            .collection(EVENT_PATH)
        return try {
            mUserChatQuery
//                .orderBy("createdDate")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "Error in addEventListener", error)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Event>()
                    value?.documents?.forEach {

                        try {
                            val tempEvent = it.toObject(Event::class.java)!!
                            if (tempEvent.date.time > now.time)
                                items.add(it.toObject(Event::class.java)!!)
                        } catch (e: Exception) {
                            Log.i(TAG, "Error when fetching events: $e")
                        }
                    }

                    onListen(items)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in addEventListener: ${e.localizedMessage}")
            null
        }
    }

    fun addEventListenerCalendar(
        onListen: (MutableList<EventSection>) -> Unit
    ): ListenerRegistration? {
        val now = Calendar.getInstance().time

        val mUserChatQuery = DB
            .collection(EVENT_PATH)
        return try {
            mUserChatQuery
//                .orderBy("createdDate")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "Error in addEventListener", error)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Event>()
                    var itemsCal = mutableListOf<EventSection>()
                    value?.documents?.forEach {

                        try {
                            val tempEvent = it.toObject(Event::class.java)!!
                            if (tempEvent.date.time > now.time)
                                items.add(it.toObject(Event::class.java)!!)
                            itemsCal = eventToCalendar(items)
                        } catch (e: Exception) {
                            Log.i(TAG, "Error when fetching events: $e")
                        }
                    }

                    onListen(itemsCal)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in addEventListener: ${e.localizedMessage}")
            null
        }
    }

    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}