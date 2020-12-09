package mfz.myfzone_sport.myf_zone.model.chat

import java.util.*

data class Message(
    var id: String,
    var senderId: String,
    var senderName: String,
    var senderClubLogo: String,
    var text: String?,
//    var image: String?,
    var createdDate: Date
) {
    constructor() : this(
        "",
        "",
        "",
        "",
//        "",
        "",
        Date(0)
    )
}