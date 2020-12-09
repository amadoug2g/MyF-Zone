package mfz.myfzone_sport.myf_zone.util.event

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.MarkerItem
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import mfz.myfzone_sport.myf_zone.util.event.OwnerUtil.getOwnerFromEvent
import mfz.myfzone_sport.myf_zone.util.event.ParticipantUtil.getParticipantsFromEvent
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

    private fun stampToDate(time: Timestamp): Date {
        return time.toDate()
    }
}