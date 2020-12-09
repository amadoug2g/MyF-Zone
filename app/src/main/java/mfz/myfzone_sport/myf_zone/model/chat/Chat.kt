package mfz.myfzone_sport.myf_zone.model.chat

import java.util.*

data class Chat(
    var coachId: String,
    var fullname: String,
    var clubLogo: String,
    var isTyping: Boolean,
    var lastMessage: String,
    var unread: Boolean,
    var createdDate: Date,
    var updatedDate: Date,
    var messages: MutableList<Message>
) {
    constructor() : this(
        "",
        "",
        "",
        false,
        "",
        false,
        Date(0),
        Date(0),
        mutableListOf()
    )

    fun toMap(): HashMap<String, Any?> {

        val result: HashMap<String, Any?> = hashMapOf(
            "coachId" to coachId,
            "fullname" to fullname,
            "clubLogo" to clubLogo,
            "isTyping" to isTyping,
            "lastMessage" to lastMessage,
            "unread" to unread,
            "createdDate" to createdDate,
            "updatedDate" to updatedDate
        )
        if (!messages.isNullOrEmpty()) {
            result["messages"] = messages
        }

        return result
    }

    fun updateToMap(): HashMap<String, Any?> {
        return hashMapOf(
            "isTyping" to isTyping,
            "lastMessage" to lastMessage,
            "unread" to unread,
            "updatedDate" to updatedDate,
            "messages" to messages
        )
    }
}