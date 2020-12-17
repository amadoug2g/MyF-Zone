package mfz.myfzone_sport.myf_zone.fragments.calendar

import android.text.format.DateFormat
import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.event.*
import mfz.myfzone_sport.myf_zone.model.event.calendar.EventSection
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Amadou on 03/12/2020, 16:49
 *
 * Calendar Page Service
 *
 */

object CalendarService {
    private val TAG = CalendarService::class.java.simpleName
    var globalEventList: MutableList<Event>? = null
    var markerItemList: MutableList<MarkerItem>? = mutableListOf()

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

    suspend fun getEventsByDate(): MutableList<Event>? {
        val docRef = DB.collection(EVENT_PATH).orderBy("date")
        val time = Calendar.getInstance().time

        return try {
            val eventList = mutableListOf<Event>()
            val documents = docRef.get().await().documents
            for (doc in documents) {
                val eventToAdd: Event = doc.toObject()!!

                if (eventToAdd.date > time) {
                    val owner =
                        getOwnerFromEvent(
                            doc.id
                        )
                    val participantList =
                        getParticipantsFromEvent(
                            doc.id
                        )

                    eventToAdd.owner = owner!!
                    eventToAdd.participants = participantList!!

                    eventList.add(eventToAdd)
                }
            }

            eventList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

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

    fun eventToCalendar(eventList: MutableList<Event>): MutableList<EventSection> {
        val list = sortedEventList(eventList)
        return mapToEventSection(list)
    }

}