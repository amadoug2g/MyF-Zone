package com.myfzone_sport.myf_zone.domain.coach

import com.myfzone_sport.myf_zone.domain.event.Event
import java.util.*

data class CoachEvent(
    var title: String,
    var description: String,
    var type: String,
    var nbTeam: Int,
    var date: Date,
    var address: String,
    var createdDate: Date
) {
    constructor() : this(
        "",
        "",
        "",
        0,
        Date(0),
        "",
        Date(0)
    )

    fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "title" to title,
            "description" to description,
            "type" to type,
            "nbTeam" to nbTeam,
            "date" to date,
            "address" to address,
            "createdDate" to createdDate
        )
    }

    fun eventToCoach(event: Event): CoachEvent {
        val newEvent = CoachEvent()
        return newEvent.apply {
            title = event.title
            description = event.description
            type = event.type
            nbTeam = event.nbTeam
            date = event.date
            address = event.address
            createdDate = event.createdDate
        }
    }

    fun updateToMap(): HashMap<String, Any?> {
        return hashMapOf(
            "title" to title,
            "description" to description,
            "type" to type,
            "nbTeam" to nbTeam,
            "date" to date,
            "address" to address
        )
    }
}