package com.example.myf_zone.model.event

import com.google.android.gms.maps.model.LatLng
import java.util.*

data class Event(
    var title: String,
    var description: String,
    var type: String,
    var nbTeam: Int,
    var date: Date,
    var address: String,
    var lat: Double,
    var lng: Double,
    var createdDate: Date,
    var owner: EventParticipation,
    var participants: MutableList<EventParticipation>
) {
    constructor() : this(
        "",
        "",
        "",
        0,
        Date(0),
        "",
        0.0,
        0.0,
        Date(0),
        EventParticipation(),
        mutableListOf()
    )

    fun getPosition(): LatLng {
        return LatLng(lat, lng)
    }

    fun getAcronym(): String {
        return this.owner.clubAcronym
    }
}