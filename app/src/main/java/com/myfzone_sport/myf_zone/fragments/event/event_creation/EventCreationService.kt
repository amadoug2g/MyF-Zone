package com.myfzone_sport.myf_zone.fragments.event.event_creation

import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.model.event.EventOwner
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB
import com.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

/**
 * Created by Amadou on 03/12/2020, 16:49
 *
 * Event Creation Page Service
 *
 */

object EventCreationService {

    fun getOwnerForEvent() =
        flow<State<Pair<EventOwner, ClubAffiliation>>> {
            val userId = ManagerAuth.activeCoach?.id
            val mClubQuery = DB
                .collection(COACH_PATH + "/${userId}/ClubAffiliation")

            emit(State.loading())

            val snapshot = mClubQuery.get().await().documents[0]
            val currentUserClub = snapshot.toObject(ClubAffiliation::class.java)!!

            val eventOwner = EventOwner().clubToOwner()

            val pair: Pair<EventOwner, ClubAffiliation> = Pair(eventOwner, currentUserClub)

            emit(State.success(pair))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(Dispatchers.IO)

    fun createEvent(event: Event) =
        flow<State<Event>> {
            val mEventQuery = DB.collection(EVENT_PATH)
            event.id = mEventQuery.document().id

            emit(State.loading())

            mEventQuery.document(event.id).set(event.toMap()).await()

            emit(State.success(event))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(Dispatchers.IO)

    fun addOwnerToEvent(event: Event, owner: EventOwner) =
        flow<State<Boolean>> {
            val mEventQuery = DB
                .document(EVENT_PATH + "/${event.id}/Owner/${owner.coachId}")

            mEventQuery.set(owner.toMap()).await()

            emit(State.success(true))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(Dispatchers.IO)

    fun addEventToUser(event: Event, owner: EventOwner, club: ClubAffiliation) =
        flow<State<Event>> {
            val mCoachEventQuery = DB
                .document(COACH_PATH + "/${owner.coachId}/ClubAffiliation/${club.clubId}/CoachEvent/${event.id}")

            emit(State.loading())

            mCoachEventQuery.set(event.toMap()).await()

            emit(State.success(event))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(Dispatchers.IO)

}