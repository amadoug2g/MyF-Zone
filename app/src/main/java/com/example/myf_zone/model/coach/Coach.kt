package com.example.myf_zone.model.coach

import com.example.myf_zone.model.chat.Chat
import java.util.*

data class Coach(
    val mail: String,
    val firstName: String,
    val lastName: String,
    val description: String,
    val devices: MutableList<String>,
    val createdDate: Date,
    val affiliations: MutableList<ClubAffiliation>,
    val chats: MutableList<Chat>
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        mutableListOf(),
        Date(0),
        mutableListOf(),
        mutableListOf()
    )
}