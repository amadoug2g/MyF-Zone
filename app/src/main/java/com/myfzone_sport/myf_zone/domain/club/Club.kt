package com.myfzone_sport.myf_zone.domain.club

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Club(
    var id: String,
    var name: String,
    var acronym: String,
    var logo: String,
    var affiliationCode: String,
    var address: String,
    var lat: Double,
    var lng: Double
) : Parcelable {
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