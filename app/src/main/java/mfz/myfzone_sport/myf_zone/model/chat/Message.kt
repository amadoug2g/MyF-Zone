package mfz.myfzone_sport.myf_zone.model.chat

import java.text.SimpleDateFormat
import java.util.*

data class Message(
    var id: String?,
    var senderId: String,
    var senderName: String,
    var senderClubLogo: String,
    var text: String?,
    var image: String?,
    var createdDate: Date
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        Date(0)
    )

    fun toMap(): HashMap<String, Any?> {
        val result: HashMap<String, Any?> = hashMapOf(
            "id" to id,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderClubLogo" to senderClubLogo,
            "createdDate" to createdDate
        )

        if (!text.isNullOrEmpty()) {
            result["text"] = text
        }

        if (!image.isNullOrEmpty()) {
            result["image"] = image
        }

        return result
    }

    val messageDate: String
        get() {
            val formatMessageDay = SimpleDateFormat("dd MMM, HH:mm", Locale.FRANCE)
            val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

            return formatMessageDay.format(formatDate.parse(createdDate.toString())!!)
        }

}