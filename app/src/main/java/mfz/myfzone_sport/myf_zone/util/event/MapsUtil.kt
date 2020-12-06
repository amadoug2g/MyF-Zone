package mfz.myfzone_sport.myf_zone.util.event

import android.content.Context
import android.graphics.BitmapFactory
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.card.MaterialCardView
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventCalendar
import mfz.myfzone_sport.myf_zone.model.event.MarkerItem
import mfz.myfzone_sport.myf_zone.model.event.calendar.EventSection
import mfz.myfzone_sport.myf_zone.util.club.ClubUtil.getClubById
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.getEventById
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.getEventsByDate
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.globalEventList
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.markerItemList
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentClub
import mfz.myfzone_sport.myf_zone.util.user.UserAffiliation.affiliationStatus
import java.text.SimpleDateFormat
import java.util.*


object MapsUtil {

    private val TAG = MapsUtil::class.java.simpleName

    suspend fun initializeMap(
        map: GoogleMap,
        markerList: MutableList<Marker>,
        cardView: MaterialCardView
    ) {
        setMapUIControls(map)
        mapStartPosition(
            map,
            markerList,
            cardView
        )
        setMapZoomPreferences(map)
    }

    suspend fun placeEventsOnMap(
        map: GoogleMap,
        context: Context,
        markerList: MutableList<Marker>
    ): MutableList<Event>? {
        return try {
            val eventList = getEventsByDate()
            if (eventList != globalEventList) {
                map.clear()
                if (!eventList.isNullOrEmpty()) {
                    for (event in eventList) {
                        val markerOptions = setEventMarkerOptions(event, context)
                        val marker = map.addMarker(markerOptions)
                        val date = event.date.time
                        markerItemList?.add(MarkerItem(marker, date))
                        Log.d(TAG, "list is $markerItemList")
                        marker.tag = event.id
                        markerList.add(marker)
                    }
                    globalEventList = eventList
//                    globalCoachEventList = getUserEvent()
//                    Log.d(TAG, "FIRST")
                    val list = sortedEventList(globalEventList!!)
                    Log.d(TAG, "Final list is: $list")
                    eventList
                } else {
                    Log.d(TAG, "List is null or empty: $eventList")
                    null
                }
            } else {
                if (!eventList.isNullOrEmpty()) {
                    for (event in eventList) {
                        val markerOptions = setEventMarkerOptions(event, context)
                        val marker = map.addMarker(markerOptions)
                        marker.tag = event.id
                        markerList.add(marker)
                        val date = event.date.time
                        markerItemList?.add(MarkerItem(marker, date))
                    }
                    val list = sortedEventList(globalEventList!!)
                    Log.d(TAG, "Final list is: $list")
                    eventList
                } else {
                    Log.d(TAG, "List is null or empty: $eventList")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getEventsByDate(): $e")
            null
        }
    }

    fun placeUserClub(clubAffiliation: ClubAffiliation, map: GoogleMap, context: Context) {
        try {
            getClubById(clubAffiliation.clubId) {

                val markerOptions = MarkerOptions().apply {
                    position(it.getPosition())
                    title(it.acronym)
                    snippet(it.name)
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
        } catch (e: Exception) {
            Log.e(TAG, "Error in placeUserClub: $e")
        }
    }

    fun getCardDetail(
        marker: Marker,
        cardView: MaterialCardView,
        title: TextView,
        description: TextView,
        image: ImageView,
        tag: TextView
    ) {
        try {
            when (marker.tag) {
                null -> {
                    cardView.visibility = View.GONE
                    tag.text = null
                }
                else -> {
                    getEventMarkerDetails(
                        marker,
                        cardView,
                        title,
                        description,
                        image,
                        tag
                    )
                    tag.text = marker.tag as String
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "an error occurred: $e")
        }

    }

    private fun sortedEventList(eventList: MutableList<Event>): MutableList<HashMap<String, MutableList<EventCalendar>>> {
        val result: MutableList<Event> = eventList
        val tempList: MutableList<EventCalendar> = mutableListOf()

        val formatEventDay = SimpleDateFormat("d", Locale.FRANCE)
        val formatEventMonth = SimpleDateFormat("MMMM", Locale.FRANCE)
        val formatEventYear = SimpleDateFormat("yyyy", Locale.FRANCE)
        val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

        val currentYear = DateFormat.format("yyyy", Date()).toString()

        result.sortBy { it.date }

        for (event in result) {

            val eventDay = formatEventDay.format(formatDate.parse(event.date.toString())!!)
            val eventMonth =
                formatEventMonth.format(formatDate.parse(event.date.toString())!!).capitalize()
//            eventMonth.capitalize(Locale.getDefault())
//            val eventMonth = formatEventMonth.format(formatDate.parse(event.date.toString())!!).toUpperCase(Locale.getDefault())
            val eventYear = formatEventYear.format(formatDate.parse(event.date.toString())!!)

            val eventDate =
                if (eventYear == currentYear) "$eventDay $eventMonth" else "$eventDay $eventMonth $eventYear"

            val calendar = EventCalendar().apply {
                id = event.id
                title = event.title
                description = event.description
                type = event.type
                nbTeam = event.nbTeam
                date = event.date
                month = eventDate
                address = event.address
                lat = event.lat
                lng = event.lng
                createdDate = event.createdDate
                owner = event.owner
                participants = event.participants
            }

            if (calendar.date > Calendar.getInstance().time)
                tempList.add(calendar)
        }

        val setMonth = mutableSetOf<String>()

        for (i in tempList)
            setMonth.add(i.month)

        val list = mutableListOf<HashMap<String, MutableList<EventCalendar>>>()

        for (month in setMonth) {
            val calendarList = mutableListOf<EventCalendar>()
            val map = hashMapOf<String, MutableList<EventCalendar>>()
            for (event in tempList) {
                if (month == event.month) {
                    calendarList.add(event)
                }
            }
            map[month] = calendarList
            list.add(map)
        }

        return list
    }

    private fun mapToEventSection(calendarList: MutableList<HashMap<String, MutableList<EventCalendar>>>): MutableList<EventSection> {
        val resultList = mutableListOf<EventSection>()

        for (item in calendarList) {
            for (j in item.keys) {
                val section = EventSection(j, item[j]!!)
                resultList.add(section)
            }
        }

        return resultList
    }

    fun eventToCalendar(eventList: MutableList<Event>): MutableList<EventSection> {
        val list = sortedEventList(eventList)
        return mapToEventSection(list)
    }

    private fun getEventMarkerDetails(
        marker: Marker,
        cardView: MaterialCardView,
        title: TextView,
        description: TextView,
        image: ImageView,
        tag: TextView
    ) {
        cardView.apply {
            visibility = View.VISIBLE
//            startAnimation(AnimationUtils.loadAnimation(context, R.anim.from_bottom))
        }

        title.text = (R.string.loading).toString()
        description.text = (R.string.loading).toString()
//        image.setImageResource(R.mipmap.ic_football_ball_icon_001)

        try {
            getEventById(marker.tag as String) { event ->
                title.text = event.title
                description.text = event.description
                image.setImageResource(
                    setMarkerType(
                        event
                    )
                )
            }
            tag.text = marker.tag as String
        } catch (e: Exception) {
            Log.e(TAG, "Error in getEventFromId: $e")
        }
    }

    fun setMarkerType(event: Event): Int {
        var result: Int = R.mipmap.ic_football_ball_icon_001
        when (event.type) {
            "friendly" -> result = R.mipmap.ic_football_ball_icon_002
            "tournament" -> result = R.mipmap.ic_football__trophy_icon_002
            "plateau" -> result = R.mipmap.ic_football_field_icon_002
        }
        return result
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
                            .decodeResource(context.resources, setMarkerType(event))
                    )
            )
        }
    }

    private fun setMapUIControls(map: GoogleMap) {
        map.uiSettings.apply {
//            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = true
        }
        map.setPadding(0, 0, 0, 146)
    }

    fun setMapZoomPreferences(
        map: GoogleMap
    ) {
        map.apply {
            setMaxZoomPreference(16f)
            setMinZoomPreference(5f)
        }
    }

    private suspend fun mapStartPosition(
        map: GoogleMap,
        markerList: MutableList<Marker>,
        cardView: MaterialCardView
    ) {
        val position = LatLng(48.8550, 2.3452)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 9.5f))

        if (map.cameraPosition.zoom > 9) {
            showMarkers(markerList)
        } else {
            hideMarkers(markerList)
            cardView.visibility = View.GONE
        }

        try {
            when (affiliationStatus()!!) {
                true -> {
                    getCurrentClub { clubAffiliation ->
                        getClubById(clubAffiliation.clubId) { club ->
                            val positionClub = LatLng(club.lat, club.lng)
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    positionClub,
                                    10.5f
                                )
                            )
                        }
                    }
                }
                false -> {
                    val positionDefault = LatLng(48.8550, 2.3452)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(positionDefault, 10.5f))
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: $e")
        }
    }

    fun hideMarkers(list: MutableList<Marker>) {
        for (i in list) {
            i.isVisible = false
        }
    }

    fun showMarkers(list: MutableList<Marker>) {
        for (i in list) {
            i.isVisible = true
        }
    }

    fun hideMarkerItem(list: MutableList<MarkerItem>) {
        for (i in list) {
            i.marker.isVisible = false
        }
    }

    fun showMarkerItem(list: MutableList<MarkerItem>) {
        for (i in list) {
            i.marker.isVisible = true
        }
    }

}