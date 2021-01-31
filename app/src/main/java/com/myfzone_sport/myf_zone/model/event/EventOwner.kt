package com.myfzone_sport.myf_zone.model.event

import com.google.firebase.auth.FirebaseUser
import com.myfzone_sport.myf_zone.model.coach.ClubAffiliation

data class EventOwner(
    var clubLogo: String,
    var clubAcronym: String,
    var coachId: String,
    var coachFullname: String,
    var sportId: String,
    var sportName: String,
    var categoryId: String,
    var categoryName: String,
    var subCategoryId: String,
    var subCategoryName: String
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    )

    fun toMap(): HashMap<String, Any?> {
        val result: HashMap<String, Any?> = hashMapOf(
            "clubLogo" to clubLogo,
            "clubAcronym" to clubAcronym,
            "coachId" to coachId,
            "coachFullname" to coachFullname,
            "sportId" to sportId,
            "sportName" to sportName
        )

        if (categoryId.isNotEmpty() && categoryName.isNotEmpty()) {
            result["categoryId"] = categoryId
            result["categoryName"] = categoryName
        }

        if (subCategoryId.isNotEmpty() && subCategoryName.isNotEmpty()) {
            result["subCategoryId"] = subCategoryId
            result["subCategoryName"] = subCategoryName
        }

        return result
    }

    fun clubToOwner(club: ClubAffiliation, coach: FirebaseUser?): EventOwner {
        val owner = EventOwner()

        owner.apply {
            clubLogo = club.clubLogo
            clubAcronym = club.clubAcronym
            coachId = coach?.uid!!
            coachFullname = coach.displayName!!
            sportId = club.sportId
            sportName = club.sportName
        }


        if (club.categoryId!!.isNotEmpty() && club.categoryName!!.isNotEmpty()) {
            owner.apply {
                categoryId = club.categoryId!!
                categoryName = club.categoryName!!
            }
        }

        if (club.subCategoryId!!.isNotEmpty() && club.subCategoryName!!.isNotEmpty()) {
            owner.apply {
                subCategoryId = club.subCategoryId!!
                subCategoryName = club.subCategoryName!!
            }
        }

        return owner
    }


}