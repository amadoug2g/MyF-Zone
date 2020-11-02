package com.example.myf_zone.model.coach

import java.util.*

data class Coach(
    val id: String,
    val mail: String,
    val firstName: String,
    val lastName: String,
//    val description: String,
//    val devices: MutableList<String>,
    val createdDate: Date
//    val affiliations: MutableList<ClubAffiliation>,
//    val chats: MutableList<Chat>
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        Date(0)
    )
}