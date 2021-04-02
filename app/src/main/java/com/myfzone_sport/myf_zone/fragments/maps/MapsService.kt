package com.myfzone_sport.myf_zone.fragments.maps

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.maps.android.clustering.ClusterManager
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.club.Club
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.model.event.EventOwner
import com.myfzone_sport.myf_zone.model.event.EventParticipant
import com.myfzone_sport.myf_zone.model.maps.BitmapHelper
import com.myfzone_sport.myf_zone.model.maps.MapClusterItem
import com.myfzone_sport.myf_zone.util.Constants.CLUB_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB
import com.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.*


/**
 * Created by Amadou on 16/12/2020
 *
 * Maps Page Service
 */

object MapsService {
    private val TAG = this::class.java.simpleName
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var clusterManager: ClusterManager<MapClusterItem>

    fun getClubById() = flow<State<Club>> {
        emit(State.loading())

        val mClubQuery = DB.document(CLUB_PATH + "/${ManagerAuth.activeCoachClub!!.clubId}")

        val snapshot = mClubQuery.get().await()
        val club: Club = snapshot.toObject()!!

        emit(State.success(club))
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
    ): MutableList<Marker>? {
        map.clear()
        val markerList = mutableListOf<Marker>()
        return if (eventList.isNotEmpty()) {
            for (event in eventList) {
                val markerOptions = setEventMarkerOptions(event, context)
                val marker = map.addMarker(markerOptions)
                marker.tag = event.id
                Log.i(TAG, "event Index = ${marker.zIndex}")
                markerList.add(marker)
            }

            markerList
        } else {
            Log.d(TAG, "List is null or empty: $eventList")
            null
        }
    }

    fun placeEventsCluster(
        map: GoogleMap,
        context: Context,
        eventList: MutableList<Event>
    ): ClusterManager<MapClusterItem>? {
        map.clear()
        return if (eventList.isNotEmpty()) {
            setUpClusters(map, context)

            for (event in eventList) {
                val markerOptions = setEventMarkerOptions(event, context)
                val marker = map.addMarker(markerOptions)
                marker.tag = event.id
                val mapClusterItem = MapClusterItem(event, marker)
//                mapClusterItem.tag = event.id
                clusterManager.addItem(mapClusterItem)
//                clusterManager.onMarkerClick(marker)
//                clusterManager.cluster()
//                clusterManager.clearItems()
            }

            clusterManager
        } else {
            Log.d(TAG, "List is null or empty: $eventList")
            null
        }
    }

    private fun setUpClusters(
        map: GoogleMap,
        context: Context
    ) {
        clusterManager = ClusterManager(context, map)
//        map.setOnCameraIdleListener(clusterManager)
//        map.setOnMarkerClickListener(clusterManager)
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

    suspend fun getOwnerFromEvent(eventId: String): EventOwner? {
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
        val homeIcon: BitmapDescriptor by lazy {
            val color = ContextCompat.getColor(context, R.color.colorAccent)
            BitmapHelper.vectorToBitmap(context, R.drawable.ic_home, color)
        }

        val markerOptions = MarkerOptions().apply {
            position(club.getPosition())
            title(club.acronym)
            snippet(club.name)
            icon(homeIcon)
//            icon(
//                BitmapDescriptorFactory
//                    .fromBitmap(
//                        BitmapFactory
//                            .decodeResource(context.resources, R.mipmap.ic_home)
//                    )
//            )
        }

        map.addMarker(markerOptions).apply {
            tag = null
            zIndex = -1f
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

    fun addEventListener(
        startDate: Long, endDate: Long,
        onListen: (MutableList<Event>) -> Unit
    ): ListenerRegistration? {

        val mUserChatQuery = DB
            .collection(EVENT_PATH)
        return try {
            mUserChatQuery
                .orderBy("date")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "Error in addEventListener", error)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Event>()
                    value?.documents?.forEach {

                        try {
                            val tempEvent = it.toObject(Event::class.java)!!
                            if (tempEvent.date.time in startDate..endDate)
                                items.add(it.toObject(Event::class.java)!!)
                        } catch (e: Exception) {
                            Log.i(TAG, "Error when fetching events: $e")
                        }
                    }

//                    items.forEach { event ->
//                        val owner = getOwnerFromEvent(event.id)
//                        val participantList = getParticipantsFromEvent(event.id)
//
//                        event.owner = owner!!
//                        event.participants = participantList!!
//                    }

                    onListen(items)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in addEventListener: ${e.localizedMessage}")
            null
        }
    }

    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}