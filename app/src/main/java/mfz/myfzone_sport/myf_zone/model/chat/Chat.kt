package mfz.myfzone_sport.myf_zone.model.chat

import java.text.SimpleDateFormat
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
            "lastMessage" to lastMessage,
            "updatedDate" to updatedDate
        )
    }

    fun updateTypingToMap(bool: Boolean): HashMap<String, Any?> {
        return hashMapOf(
            "isTyping" to bool
        )
    }

    val chatDate: String
        get() {
            val currentTime = Calendar.getInstance().time
            val formatDate = SimpleDateFormat("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

            val checkFormatMessageDay = SimpleDateFormat("D", Locale.FRANCE)
            val checkFormatMessageMonth = SimpleDateFormat("w", Locale.FRANCE)
            val checkFormatMessageYear = SimpleDateFormat("yyyy", Locale.FRANCE)

            val currentDay =
                checkFormatMessageDay.format(formatDate.parse(currentTime.toString())!!)
            val currentWeek =
                checkFormatMessageMonth.format(formatDate.parse(currentTime.toString())!!)
            val currentYear =
                checkFormatMessageYear.format(formatDate.parse(currentTime.toString())!!)

            val formatMessageHour = SimpleDateFormat("HH:mm", Locale.FRANCE)
            val formatMessageDay = SimpleDateFormat("EEE", Locale.FRANCE)
            val formatMessageYear = SimpleDateFormat("dd MMM", Locale.FRANCE)
            val formatMessageDefault = SimpleDateFormat("dd/MM/y", Locale.FRANCE)

            var result = formatMessageDefault.format(formatDate.parse(updatedDate.toString())!!)

            val boolDay =
                currentDay == checkFormatMessageDay.format(formatDate.parse(updatedDate.toString())!!)
            val boolWeek =
                currentWeek == checkFormatMessageMonth.format(formatDate.parse(updatedDate.toString())!!)
            val boolYear =
                currentYear == checkFormatMessageYear.format(formatDate.parse(updatedDate.toString())!!)

            if (boolDay) {
                result = formatMessageHour.format(formatDate.parse(updatedDate.toString())!!)
            } else {
                if (boolWeek) {
                    result = formatMessageDay.format(formatDate.parse(updatedDate.toString())!!)
                } else if (boolYear) {
                    result = formatMessageYear.format(formatDate.parse(updatedDate.toString())!!)
                }
            }

            return result
        }
}