package com.myfzone_sport.myf_zone.domain.event

import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliation
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth

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

    fun clubToOwner(): EventOwner {
        val owner = EventOwner()

        owner.apply {
            clubLogo = activeCoachClubAffiliation!!.clubLogo
            clubAcronym = activeCoachClubAffiliation!!.clubAcronym
            coachId = activeCoach?.id!!
            coachFullname = activeCoach!!.getName()
            sportId = activeCoachClubAffiliation!!.sportId
            sportName = activeCoachClubAffiliation!!.sportName
        }


        if (activeCoachClubAffiliation!!.categoryId!!.isNotEmpty() && activeCoachClubAffiliation!!.categoryName!!.isNotEmpty()) {
            owner.apply {
                categoryId = activeCoachClubAffiliation!!.categoryId!!
                categoryName = activeCoachClubAffiliation!!.categoryName!!
            }
        }

        if (activeCoachClubAffiliation!!.subCategoryId!!.isNotEmpty() && activeCoachClubAffiliation!!.subCategoryName!!.isNotEmpty()) {
            owner.apply {
                subCategoryId = activeCoachClubAffiliation!!.subCategoryId!!
                subCategoryName = activeCoachClubAffiliation!!.subCategoryName!!
            }
        }

        return owner
    }


}