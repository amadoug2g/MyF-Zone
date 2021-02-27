package com.myfzone_sport.myf_zone.fragments.event.event_details.guest

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.model.event.EventOwner
import com.myfzone_sport.myf_zone.util.Constants.DB
import com.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

/**
 * Created by Amadou on 31/01/2021, 16:43
 *
 * Event Details Guest Page Service
 *
 */

object EventDetailsGuestService {
    private val TAG = this::class.java.simpleName

    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun getEvent(eventId: String) = flow<State<Event>> {
        val mEventQuery = DB.document(EVENT_PATH + "/${eventId}")

        emit(State.loading())

        val snapshot = mEventQuery.get().await()
        val event = snapshot.toObject(Event::class.java)

        emit(State.success(event!!))
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

    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}