package com.example.myf_zone.model.event

import com.example.myf_zone.R
import com.google.android.gms.maps.model.LatLng
import java.util.*

data class Event(
    var id: String,
    var title: String,
    var description: String,
    var type: String,
    var nbTeam: Int,
    var date: Date,
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
        0.0,
        0.0,
        Date(0),
        EventOwner(),
        mutableListOf()
    )

    fun getPosition(): LatLng {
        return LatLng(lat, lng)
    }

    fun getAcronym(): String {
        return owner.clubAcronym
    }

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
            var result: Int = R.mipmap.ic_football_ball_icon_001
            when (type) {
                "friendly" -> result = R.mipmap.ic_football_ball_icon_002
                "tournament" -> result = R.mipmap.ic_football__trophy_icon_002
                "plateau" -> result = R.mipmap.ic_football_field_icon_002
            }
            return result
        }

    override fun toString(): String {

//        return "$title - $eventTypeString, se déroulera à $address le $date"

        return "Titre: $title" +
                "Description: $description" +
                "Type: $eventTypeString" +
                "Nombre d'équipe: $nbTeam" +
                "Date de l'évènement: $date" +
                "Adresse: $address " +
                "Latitude: $lat" +
                "Longitude: $lng" +
                "Date de création: $createdDate" +
                "Owner: $owner" +
                "Participant(s) $participants"
    }
}