package com.myfzone_sport.myf_zone.fragments.event.event_edit

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB
import com.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

/**
 * Created by Amadou on 02/12/2020, 21:11
 *
 * Event Edit Page Service
 *
 */

object EventEditService {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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

    fun getUserClub() = flow<State<ClubAffiliation>> {
        val userId = firebaseAuth.currentUser?.uid
        val mClubQuery = DB
            .collection(COACH_PATH + "/${userId}/ClubAffiliation")

        emit(State.loading())

        val snapshot = mClubQuery.get().await().documents[0]
        val currentUserClub = snapshot.toObject(ClubAffiliation::class.java)

        emit(State.success(currentUserClub!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun updateEvent(event: Event) = flow<State<Boolean>> {
        val mEventUpdateQuery = DB
            .document(EVENT_PATH + "/${event.id}")

        emit(State.loading())

        mEventUpdateQuery.set(event.updateToMap(), SetOptions.merge()).await()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun updateEventForOwner(event: Event, clubAffiliation: ClubAffiliation) = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid
        val mEventUpdateQuery = DB
            .document(COACH_PATH + "/${userId}/ClubAffiliation/${clubAffiliation.clubId}/CoachEvent/${event.id}")

        emit(State.loading())

        mEventUpdateQuery.set(event.updateToMap(), SetOptions.merge()).await()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)
}