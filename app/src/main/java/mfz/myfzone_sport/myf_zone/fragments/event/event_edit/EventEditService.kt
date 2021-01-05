package mfz.myfzone_sport.myf_zone.fragments.event.event_edit

import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventParticipant
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH

/**
 * Created by Amadou on 02/12/2020, 21:11
 *
 * Event Edit Page Service
 *
 */

object EventEditService {

    fun getEvent(eventId: String) = flow<State<Event>> {
        val mEventQuery = DB.document(EVENT_PATH + "/${eventId}")

        emit(State.loading())

        val snapshot = mEventQuery.get().await()
        val event = snapshot.toObject(Event::class.java)

        emit(State.success(event!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun getEventParticipant(eventId: String) = flow<State<MutableList<EventParticipant>>> {
        emit(State.loading())

        val mParticipantList = DB.collection(EVENT_PATH + "/${eventId}/Participant")

        val snapshot = mParticipantList.get().await()

        val resultState =
            if (!snapshot.isEmpty) (State.success(snapshot.toObjects(EventParticipant::class.java))) else (State.success(
                mutableListOf()
            ))

        emit(resultState)
//        emit(State.success(participantList))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun updateEvent(eventId: String, event: Event) = flow<State<Boolean>> {
        val mEventUpdateQuery = DB
            .document(EVENT_PATH + "/${eventId}")

        emit(State.loading())

        mEventUpdateQuery.set(event.updateToMap(), SetOptions.merge()).await()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)
}