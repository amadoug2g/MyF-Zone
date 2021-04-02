package com.myfzone_sport.myf_zone.fragments.event.event_details.owner

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.model.event.EventOwner
import com.myfzone_sport.myf_zone.model.event.EventParticipant
import com.myfzone_sport.myf_zone.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

/**
 * Created by Amadou on 31/01/2021, 17:42
 *
 * Event Details Owner Page Service
 *
 */
object EventDetailsOwnerService {
    private val TAG = this::class.java.simpleName

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

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

    fun acceptParticipant(eventId: String, participant: EventParticipant) =
        flow<State<EventParticipant>> {
            val mAcceptParticipant = Constants.DB
                .document(Constants.EVENT_PATH + "/${eventId}/Participant/${participant.coachId}")
            val status = setParticipantAccepted()

            emit(State.loading())

            mAcceptParticipant.set(status, SetOptions.merge()).await()

            emit(State.success(participant))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(Dispatchers.IO)

    private fun setParticipantAccepted(): HashMap<String, Any?> {
        return hashMapOf(
            "status" to "validate"
        )
    }

    fun refuseParticipant(eventId: String, participant: EventParticipant) =
        flow<State<EventParticipant>> {
            val mAcceptParticipant = Constants.DB
                .document(Constants.EVENT_PATH + "/${eventId}/Participant/${participant.coachId}")
            val status = setParticipantRefused()

            emit(State.loading())

            mAcceptParticipant.set(status, SetOptions.merge()).await()

            emit(State.success(participant))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(Dispatchers.IO)

    private fun setParticipantRefused(): HashMap<String, Any?> {
        return hashMapOf(
            "status" to "refused"
        )
    }

    suspend fun getValidParticipantCount(eventId: String): String {
        val participantList = getParticipantsFromEvent(eventId)
        return try {
            when (participantList.isNullOrEmpty()) {
                true -> {
                    "0"
                }
                false -> {
                    var result = 0
                    for (item in participantList) {
                        if (item.status == "validate")
                            result++
                    }

                    result.toString()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getValidParticipantCount: $e")
            "?"
        }
    }

    private suspend fun getParticipantsFromEvent(eventId: String): MutableList<EventParticipant>? {
        val docRef =
            Constants.DB.collection(Constants.EVENT_PATH)
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

    fun deleteEvent(eventId: String) = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid
        emit(State.loading())

        val mEventQuery = Constants.DB.document(Constants.EVENT_PATH + "/${eventId}")
        val mParticipantListQuery =
            Constants.DB.collection(Constants.EVENT_PATH + "/${eventId}/Participant")
        val mOwnerQuery =
            Constants.DB.document(Constants.EVENT_PATH + "/${eventId}/Owner/${userId}")
        val mOwnerEventQuery =
            Constants.DB.document(Constants.COACH_PATH + "/${userId}/ClubAffiliation/${ManagerAuth.activeCoachClub!!.clubId}/CoachEvent/${eventId}")

        mParticipantListQuery.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.documents.size > 0) {
                val participantList = mutableListOf<EventParticipant>()

                querySnapshot.forEach { participant ->
                    participantList.add(participant.toObject())
                }

                participantList.forEach { coach ->
                    val mParticipantQuery =
                        Constants.DB.document(Constants.EVENT_PATH + "/${eventId}/Participant/${coach.coachId}")
                    mParticipantQuery.delete()
                }

            } else return@addOnSuccessListener
        }

        mOwnerQuery.delete()
        mEventQuery.delete()
        mOwnerEventQuery.delete()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}