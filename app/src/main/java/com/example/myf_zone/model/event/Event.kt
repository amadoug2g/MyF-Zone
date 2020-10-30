package com.example.myf_zone.model.event

import com.google.android.gms.maps.model.LatLng
import java.util.*

data class Event(
    val title: String,
    val description: String,
    val type: String,
    val nbTeam: Int,
    val date: Date,
    val address: String,
    val lat: Double,
    val lng: Double,
    val createdDate: Date,
    val owner: EventParticipation,
    val participants: MutableList<EventParticipation>
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