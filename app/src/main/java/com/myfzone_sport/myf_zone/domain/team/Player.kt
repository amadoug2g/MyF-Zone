package com.myfzone_sport.myf_zone.domain.team

import java.util.*

data class Player(
    val firstName: String,
    val lastName: String,
    val licenceId: String,
    val createdDate: Date
) {
    constructor() : this(
        "",
        "",
        "",
        Date(0)
    )
}