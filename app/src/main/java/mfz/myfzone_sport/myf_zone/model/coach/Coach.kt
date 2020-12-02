package mfz.myfzone_sport.myf_zone.model.coach

import java.util.*

data class Coach(
    var id: String,
    var mail: String,
    var firstName: String,
    var lastName: String,
//    val description: String,
//    val devices: MutableList<String>,
    var createdDate: Date
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

    fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "mail" to mail,
            "firstName" to firstName,
            "lastName" to lastName,
            "id" to id,
            "createdDate" to createdDate
        )
    }

    fun getName(): String {
        return "$firstName $lastName"
    }
}