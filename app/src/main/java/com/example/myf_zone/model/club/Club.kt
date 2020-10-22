package com.example.myf_zone.model.club

import java.util.*

data class Club(
    val name: String,
    val acronym: String,
    val logo: String,
    val affiliationCode: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val createDate: Date,
    val affiliationRequests: MutableList<AffiliationRequest>
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        0.0,
        0.0,
        Date(0),
        mutableListOf()
    )
}