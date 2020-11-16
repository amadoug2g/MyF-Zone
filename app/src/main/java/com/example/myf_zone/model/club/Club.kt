package com.example.myf_zone.model.club

import com.google.android.gms.maps.model.LatLng

data class Club(
    var id: String,
    var name: String,
    var acronym: String,
    var logo: String,
    var affiliationCode: String,
    var address: String,
    var lat: Double,
    var lng: Double
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        0.0,
        0.0
    )

    fun getPosition(): LatLng {
        return LatLng(lat, lng)
    }
}