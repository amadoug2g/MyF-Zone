package com.myfzone_sport.myf_zone.model.club

import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import java.util.*

/**
 * Created by Amadou on 26/04/2021, 19:07
 *
 * Club Suggestion Data Class
 *
 */

data class ClubSuggestion(
    var id: String,
    var name: String,
    var acronym: String,
    var address: String,
    var lat: Double,
    var lng: Double,
    var createdDate: Date,
    var coachId: String?,
    var coachFullname: String?
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        0.0,
        0.0,
        Date(0),
        "",
        ""
    )

    fun toMap(): HashMap<String, Any?> {
        val result: HashMap<String, Any?> = hashMapOf(
            "id" to id,
            "name" to name,
            "acronym" to acronym,
            "address" to address,
            "lat" to lat,
            "lng" to lng,
            "createdDate" to createdDate
        )

        if (ManagerAuth.isConnected) {
            result["coachId"] to ManagerAuth.activeCoach?.id
            result["coachFullname"] to ManagerAuth.activeCoach?.getName()
        }

        return result
    }

    fun toMapSug(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "acronym" to acronym,
            "address" to address,
            "lat" to lat,
            "lng" to lng,
            "createdDate" to createdDate,
            "coachId" to ManagerAuth.activeCoach?.id,
            "coachFullname" to ManagerAuth.activeCoach?.getName()
        )
    }
}