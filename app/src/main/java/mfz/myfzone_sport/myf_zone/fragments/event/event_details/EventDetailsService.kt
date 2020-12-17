package mfz.myfzone_sport.myf_zone.fragments.event.event_details

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventOwner
import mfz.myfzone_sport.myf_zone.model.event.EventParticipant
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH

/**
 * Created by Amadou on 03/12/2020, 16:45
 *
 * Event Details Page Service
 *
 */

object EventDetailsService {
    private val TAG = EventDetailsService::class.java.simpleName

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun getCurrentUser() = flow<State<Coach>> {
        val userId = firebaseAuth.currentUser?.uid
        val mUserQuery = DB.document(COACH_PATH + "/${userId}")

        emit(State.loading())

        val snapshot = mUserQuery.get().await()
        val currentUser = snapshot.toObject(Coach::class.java)

        emit(State.success(currentUser!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun getEvent(eventId: String) = flow<State<Event>> {
        val mEventQuery = DB.document(EVENT_PATH + "/${eventId}")

        emit(State.loading())

        val snapshot = mEventQuery.get().await()
        val event = snapshot.toObject(Event::class.java)

        emit(State.success(event!!))
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

    fun getOwnerFromEvent(eventId: String) = flow<State<EventOwner>> {
        val mEventOwnerQuery = DB
            .collection(EVENT_PATH + "/${eventId}/Owner")

        emit(State.loading())

        val snapshot = mEventOwnerQuery.get().await().documents[0]
        val eventOwner = snapshot.toObject(EventOwner::class.java)

        emit(State.success(eventOwner!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun getEventParticipant(eventId: String) = flow<State<MutableList<EventParticipant>>> {
        val mParticipantList = DB.collection(EVENT_PATH + "/${eventId}/Participant")

        emit(State.loading())

        val snapshot = mParticipantList.get().await().documents
        val participantList = mutableListOf<EventParticipant>()
        snapshot.forEach { participantList.add(it.toObject()!!) }

        emit(State.success(participantList))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun getParticipantQuery(eventId: String) = flow<State<CollectionReference>> {
        emit(State.loading())

        val mQuery = fireStoreInstance.collection(EVENT_PATH + "/${eventId}/Participant")

        emit(State.success(mQuery))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun addParticipant(eventId: String, participant: EventParticipant) =
        flow<State<EventParticipant>> {
            val mParticipantQuery = DB
                .document(EVENT_PATH + "/${eventId}/Participant/${participant.coachId}")

            emit(State.loading())

            mParticipantQuery.set(participant.toMap()).await()

            emit(State.success(participant))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(IO)

    fun removeParticipant(eventId: String) = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid
        val mParticipantQuery = DB
            .document(EVENT_PATH + "/${eventId}/Participant/${userId}")

        emit(State.loading())

        mParticipantQuery.delete().await()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun acceptParticipant(eventId: String, participant: EventParticipant) =
        flow<State<EventParticipant>> {
            val mAcceptParticipant = DB
                .document(EVENT_PATH + "/${eventId}/Participant/${participant.coachId}")
            val status = setParticipantAccepted()

            emit(State.loading())

            mAcceptParticipant.set(status, SetOptions.merge()).await()

            emit(State.success(participant))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(IO)

    private fun setParticipantAccepted(): HashMap<String, Any?> {
        return hashMapOf(
            "status" to "validate"
        )
    }

    fun refuseParticipant(eventId: String, participant: EventParticipant) =
        flow<State<EventParticipant>> {
            val mAcceptParticipant = DB
                .document(EVENT_PATH + "/${eventId}/Participant/${participant.coachId}")
            val status = setParticipantRefused()

            emit(State.loading())

            mAcceptParticipant.set(status, SetOptions.merge()).await()

            emit(State.success(participant))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(IO)

    private fun setParticipantRefused(): HashMap<String, Any?> {
        return hashMapOf(
            "status" to "refused"
        )
    }

    fun checkAffiliationStatus() = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid
        val mAffiliationPath = DB
            .collection(COACH_PATH + "/${userId}/ClubAffiliation")

        emit(State.loading())

        val snapshot = mAffiliationPath.get().await()
        val status = snapshot.documents.size > 0

        emit(State.success(status))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun checkUserParticipation(eventId: String) = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid
        val mParticipantListQuery = DB.collection(EVENT_PATH + "/${eventId}/Participant")

        emit(State.loading())

        val snapshot = mParticipantListQuery.get().await().documents
        val participantList = mutableListOf<EventParticipant>()
        snapshot.forEach { participantList.add(it.toObject()!!) }

        participantList.forEach { if (it.coachId == userId) emit(State.success(true)) }
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

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

    fun deleteAllEvent(eventId: String, club: ClubAffiliation) = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid

        emit(State.loading())

        val mEventQuery = DB.document(EVENT_PATH + "/${eventId}")
        val mParticipantListQuery = DB.collection(EVENT_PATH + "/${eventId}/Participant")
        val mOwnerQuery = DB.document(EVENT_PATH + "/${eventId}/Owner/${userId}")
        val mOwnerEventQuery =
            DB.document(COACH_PATH + "/${userId}/ClubAffiliation/${club.clubId}/CoachEvent/${eventId}")

        Log.i("EventDetailsService", "Event Path: ${mEventQuery.path}")
        Log.d(TAG, "deleteEventForOwner path: ${mOwnerEventQuery.path}")
        Log.d(TAG, "deleteEventForOwner path id: ${mOwnerEventQuery.id}")
        Log.d(TAG, "deleteEventForOwner get: ${mOwnerEventQuery.get()}")

        val snapshot = mParticipantListQuery.get().await().documents
        val participantList = mutableListOf<EventParticipant>()
        snapshot.forEach { participantList.add(it.toObject()!!) }
        if (participantList.isNotEmpty()) {
            participantList.forEach {
                deleteParticipant(eventId, it.coachId).collect { state ->
                    when (state) {
                        is State.Loading -> {
                        }
                        is State.Success -> {
                        }
                        is State.Failed -> {
                        }
                    }
                }
            }
        }

        mOwnerEventQuery.delete()

        mOwnerQuery.delete()

        mEventQuery.delete()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun deleteEvent(eventId: String, club: ClubAffiliation) = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid

        emit(State.loading())

        val mEventQuery = DB.document(EVENT_PATH + "/${eventId}")
        val mParticipantListQuery = DB.collection(EVENT_PATH + "/${eventId}/Participant")
        val mOwnerQuery = DB.document(EVENT_PATH + "/${eventId}/Owner/${userId}")
        val mOwnerEventQuery =
            DB.document(COACH_PATH + "/${userId}/ClubAffiliation/${club.clubId}/CoachEvent/${eventId}")

        mParticipantListQuery.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.documents.size > 0) {
                val participantList = mutableListOf<EventParticipant>()

                querySnapshot.forEach { participant ->
                    participantList.add(participant.toObject())
                }

                participantList.forEach { coach ->
                    val mParticipantQuery =
                        DB.document(EVENT_PATH + "/${eventId}/Participant/${coach.coachId}")
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
    }.flowOn(IO)

    fun deleteParticipants(eventId: String) = flow<State<Boolean>> {
        val mParticipantListQuery = DB.collection(EVENT_PATH + "/${eventId}/Participant")

        emit(State.loading())

        val snapshot = mParticipantListQuery.get().await().documents
        val participantList = mutableListOf<EventParticipant>()
        snapshot.forEach { participantList.add(it.toObject()!!) }

        if (participantList.isNotEmpty()) {
            participantList.forEach {
                deleteParticipant(eventId, it.coachId).collect { state ->
                    when (state) {
                        is State.Loading -> {
                        }
                        is State.Success -> {
                        }
                        is State.Failed -> {
                        }
                    }
                }
            }
        }
        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    private fun deleteParticipant(eventId: String, coachId: String) = flow<State<Boolean>> {
        val mParticipantQuery = DB.document(EVENT_PATH + "/${eventId}/Participant/${coachId}")

        emit(State.loading())

        mParticipantQuery.delete()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun deleteOwner(eventId: String) = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid

        val mOwnerQuery = DB.document(EVENT_PATH + "/${eventId}/Owner/${userId}")

        emit(State.loading())

        mOwnerQuery.delete()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun deleteEventForOwner(eventId: String, club: ClubAffiliation) = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid

        Log.d(TAG, "deleteEventForOwner id: $userId")

        val mOwnerEventQuery =
            DB.document(COACH_PATH + "/${userId}/ClubAffiliation/${club.clubId}/CoachEvent/${eventId}")

        Log.d(TAG, "deleteEventForOwner path: ${mOwnerEventQuery.path}")
        Log.d(TAG, "deleteEventForOwner path id: ${mOwnerEventQuery.id}")
        Log.d(TAG, "deleteEventForOwner get: ${mOwnerEventQuery.get()}")

        emit(State.loading())

        mOwnerEventQuery.delete()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}