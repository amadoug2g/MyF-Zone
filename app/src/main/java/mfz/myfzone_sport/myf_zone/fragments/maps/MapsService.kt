package mfz.myfzone_sport.myf_zone.fragments.maps

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.club.Club
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventOwner
import mfz.myfzone_sport.myf_zone.model.event.EventParticipant
import mfz.myfzone_sport.myf_zone.util.Constants.CLUB_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH


/**
 * Created by Amadou on 16/12/2020
 *
 * Maps Page Service
 */

object MapsService {
    private val TAG = MapsService::class.java.simpleName
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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

    fun getClubById(clubAffiliation: ClubAffiliation) = flow<State<Club>> {
        emit(State.loading())

        val mClubQuery = DB.document(CLUB_PATH + "/${clubAffiliation.clubId}")

        val snapshot = mClubQuery.get().await()
        val club: Club = snapshot.toObject()!!

        emit(State.success(club))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

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

    fun initializeMap(
        map: GoogleMap,
        club: Club? = null
    ) {
        setMapUIControls(map)
        mapStartPosition(map, club)
        setMapZoomPreferences(map)
    }

    private fun setMapUIControls(map: GoogleMap) {
        map.uiSettings.apply {
            isCompassEnabled = false
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = true
        }
        map.setPadding(0, 0, 0, 146)
    }

    private fun mapStartPosition(
        map: GoogleMap,
        club: Club? = null
    ) {
        val position = LatLng(48.8550, 2.3452)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 9.5f))

        if (club != null) {
            val positionClub = LatLng(club.lat, club.lng)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(positionClub, 10.5f))
        }

    }

    private fun setMapZoomPreferences(
        map: GoogleMap
    ) {
        map.apply {
            setMaxZoomPreference(16f)
            //setMinZoomPreference(5f)
        }
    }

    fun placeEvents(
        map: GoogleMap,
        context: Context,
        eventList: MutableList<Event>
    ): MutableList<Event>? {
        map.clear()
        return if (eventList.isNotEmpty()) {
            for (event in eventList) {
                val markerOptions = setEventMarkerOptions(event, context)
                val marker = map.addMarker(markerOptions)
                marker.tag = event.id
            }

            eventList
        } else {
            Log.d(TAG, "List is null or empty: $eventList")
            null
        }
    }

    fun getEvents(startDate: Long, endDate: Long) = flow<State<MutableList<Event>>> {
        emit(State.loading())

        val mEventQuery = DB.collection(EVENT_PATH).orderBy("date")

        val snapshot = mEventQuery.get().await()
        val eventList: MutableList<Event> = mutableListOf()

        snapshot.forEach {
            val event = it.toObject<Event>()
            if (event.date.time in startDate..endDate) eventList.add(event)
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
    }.flowOn(IO)

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

    fun placeUserClub(club: Club, map: GoogleMap, context: Context) {
        val markerOptions = MarkerOptions().apply {
            position(club.getPosition())
            title(club.acronym)
            snippet(club.name)
            icon(
                BitmapDescriptorFactory
                    .fromBitmap(
                        BitmapFactory
                            .decodeResource(context.resources, R.mipmap.ic_home)
                    )
            )
        }
        map.addMarker(markerOptions).apply {
            tag = null
        }
    }

    private fun setEventMarkerOptions(event: Event, context: Context): MarkerOptions {
        return MarkerOptions().apply {
            position(event.getPosition())
            title(event.getAcronym())
            snippet(event.title)
            icon(
                BitmapDescriptorFactory
                    .fromBitmap(
                        BitmapFactory
                            .decodeResource(context.resources, event.eventTypeImage)
                    )
            )
        }
    }

    fun getEventById(eventId: String) = flow<State<Event>> {
        val mEventIdQuery = DB.document(EVENT_PATH + "/${eventId}")

        emit(State.loading())

        val snapshot = mEventIdQuery.get().await()
        val event: Event = snapshot.toObject()!!

        emit(State.success(event))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)
}