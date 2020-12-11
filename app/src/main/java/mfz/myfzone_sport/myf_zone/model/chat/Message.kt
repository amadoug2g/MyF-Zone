package mfz.myfzone_sport.myf_zone.model.chat

import java.text.SimpleDateFormat
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

    fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderClubLogo" to senderClubLogo,
            "text" to text,
            "createdDate" to createdDate
        )
    }

    val messageDate: String
        get() {
            val formatMessageDay = SimpleDateFormat("dd MMM, HH:mm", Locale.FRANCE)
            val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

            return formatMessageDay.format(formatDate.parse(createdDate.toString())!!)
        }

}