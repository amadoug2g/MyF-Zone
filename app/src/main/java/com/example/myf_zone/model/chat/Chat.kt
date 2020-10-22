package com.example.myf_zone.model.chat

import java.util.*

data class Chat(
    val coachId: String,
    val fullName: String,
    val clubLogo: String,
    val isTyping: Boolean,
    val lastMessage: String,
    val unread: String,
    val createdDate: Date
) {
    constructor() : this(
        "",
        "",
        "",
        false,
        "",
        "",
        Date(0)
    )
}