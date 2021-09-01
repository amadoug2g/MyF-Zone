package com.myfzone_sport.myf_zone.model.event

import com.myfzone_sport.myf_zone.R
import java.text.SimpleDateFormat
import java.util.*

data class EventCalendar(
    var id: String,
    var title: String,
    var description: String,
    var type: String,
    var nbTeam: Int,
    var date: Date,
    var month: String,
    var address: String,
    var lat: Double,
    var lng: Double,
    var createdDate: Date,
    var owner: EventOwner,
    var participants: MutableList<EventParticipant>
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        0,
        Date(0),
        "",
        "",
        0.0,
        0.0,
        Date(0),
        EventOwner(),
        mutableListOf()
    )

    var eventTypeString: String
        get() {
            var result = ""
            when (type) {
                "friendly" -> result = "Match amical"
                "tournament" -> result = "Tournoi"
                "plateau" -> result = "Plateau"
//                "friendly" -> result = Resources.getSystem().getString(R.string.friendly_event)
//                "tournament" -> result = Resources.getSystem().getString(R.string.tournament_event)
//                "plateau" -> result = Resources.getSystem().getString(R.string.plateau_event)
            }
            return result
        }
        set(value) {
            type = value
        }

    val eventTypeImage: Int
        get() {
            var result: Int = R.mipmap.ic_logo_icon
            when (type) {
                "friendly" -> result = R.mipmap.ic_ball
                "tournament" -> result = R.mipmap.ic_cup
                "plateau" -> result = R.mipmap.ic_stadium
            }
            return result
        }

    fun getEvent(): String {
        return "$title " +
                "\n$description" +
                "\n$eventTypeString" +
                "\n$nbTeam" +
                "\n$address" //+ "\n$owner" + "\n$participants"
    }

    val eventDateToDayProfile: String
        get() {
            val formatEventDay = SimpleDateFormat("dd MMMM y", Locale.FRANCE)
            val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

            return formatEventDay.format(formatDate.parse(date.toString())!!)
        }

    override fun toString(): String {
        return "Le $eventTypeString \"$title\" se déroulera à [$address] le $date [$month]"
    }
}