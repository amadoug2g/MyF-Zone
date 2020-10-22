package com.example.myf_zone.model.coach

import java.util.*

data class CoachEvent(
    val title: String,
    val description: String,
    val type: String,
    val nbTeam: Int,
    val date: Date,
    val address: String,
    val createdDate: Date
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
}