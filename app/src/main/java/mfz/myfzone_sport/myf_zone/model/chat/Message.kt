package mfz.myfzone_sport.myf_zone.model.chat

import java.util.*

data class Message(
    val senderId: String,
    val senderName: String,
    val senderClubLogo: String,
    val text: String?,
    val image: String?,
    val createdDate: Date
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        Date(0)
    )
}