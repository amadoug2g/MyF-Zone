package com.myfzone_sport.myf_zone.fragments.event.event_details.participant

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

/**
 * Created by Amadou on 29/03/2021, 16:04
 *
 * Event Details Participant Page Service
 *
 */

object EventDetailsParticipantService {

    private val TAG = this::class.java.simpleName

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun getOwnerToken(ownerId: String) = flow<State<MutableList<String>>> {
        val mOwnerTokenQuery = Constants.DB.document(Constants.COACH_PATH + "/${ownerId}")

        val snapshot = mOwnerTokenQuery.get().await()
        val user: Coach = snapshot.toObject()!!

        val tokenList = mutableListOf<String>()

        if (!user.devices.isNullOrEmpty()) {
            user.devices.forEach { tokenList.add(it) }
            emit(State.success(tokenList))
            Log.i(TAG, "Tokens: $tokenList")
        } else {
            emit(State.success(mutableListOf()))
            Log.i(TAG, "Tokens: list is empty")
        }

    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun getEvent(eventId: String) = flow<State<Event>> {
        val mEventQuery = Constants.DB.document(Constants.EVENT_PATH + "/${eventId}")

        emit(State.loading())

        val snapshot = mEventQuery.get().await()
        val event = snapshot.toObject(Event::class.java)

        emit(State.success(event!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun getOwnerFromEvent(eventId: String) = flow<State<EventOwner>> {
        val mEventOwnerQuery = Constants.DB
            .collection(Constants.EVENT_PATH + "/${eventId}/Owner")

        emit(State.loading())

        val snapshot = mEventOwnerQuery.get().await().documents[0]
        val eventOwner = snapshot.toObject(EventOwner::class.java)

        emit(State.success(eventOwner!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun getEventParticipant(eventId: String) = flow<State<MutableList<EventParticipant>>> {
        emit(State.loading())

        val mParticipantList =
            Constants.DB.collection(Constants.EVENT_PATH + "/${eventId}/Participant")

        val snapshot = mParticipantList.get().await()

        val resultState =
            if (!snapshot.isEmpty) (State.success(snapshot.toObjects(EventParticipant::class.java))) else (State.success(
                mutableListOf()
            ))

        emit(resultState)
//        emit(State.success(participantList))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun addParticipant(eventId: String, participant: EventParticipant) =
        flow<State<EventParticipant>> {
            val mParticipantQuery = Constants.DB
                .document(Constants.EVENT_PATH + "/${eventId}/Participant/${participant.coachId}")

            emit(State.loading())

            mParticipantQuery.set(participant.toMap()).await()

            emit(State.success(participant))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(Dispatchers.IO)

    fun removeParticipant(eventId: String) = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid
        val mParticipantQuery = Constants.DB
            .document(Constants.EVENT_PATH + "/${eventId}/Participant/${userId}")

        emit(State.loading())

        mParticipantQuery.delete().await()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun checkUserParticipation(eventId: String) = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid
        val mParticipantListQuery =
            Constants.DB.collection(Constants.EVENT_PATH + "/${eventId}/Participant")

        emit(State.loading())

        val snapshot = mParticipantListQuery.get().await().documents
        val participantList = mutableListOf<EventParticipant>()
        snapshot.forEach { participantList.add(it.toObject()!!) }

        participantList.forEach { if (it.coachId == userId) emit(State.success(true)) }
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}