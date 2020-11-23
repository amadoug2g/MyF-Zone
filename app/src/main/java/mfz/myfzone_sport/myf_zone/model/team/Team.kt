package mfz.myfzone_sport.myf_zone.model.team

import java.util.*

data class Team(
    val title: String,
    val createdDate: Date,
    val players: MutableList<Player>
) {
    constructor() : this(
        "",
        Date(0),
        mutableListOf()
    )
}