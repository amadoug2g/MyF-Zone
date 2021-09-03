package com.myfzone_sport.myf_zone.domain.event

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
            clubLogo = ManagerAuth.activeCoachClubAffiliation!!.clubLogo
            clubAcronym = ManagerAuth.activeCoachClubAffiliation!!.clubAcronym
            coachId = ManagerAuth.activeCoach?.id!!
            coachFullname = ManagerAuth.activeCoach!!.getName()
            sportId = ManagerAuth.activeCoachClubAffiliation!!.sportId
            sportName = ManagerAuth.activeCoachClubAffiliation!!.sportName
        }


        if (ManagerAuth.activeCoachClubAffiliation!!.categoryId!!.isNotEmpty() && ManagerAuth.activeCoachClubAffiliation!!.categoryName!!.isNotEmpty()) {
            owner.apply {
                categoryId = ManagerAuth.activeCoachClubAffiliation!!.categoryId!!
                categoryName = ManagerAuth.activeCoachClubAffiliation!!.categoryName!!
            }
        }

        if (ManagerAuth.activeCoachClubAffiliation!!.subCategoryId!!.isNotEmpty() && ManagerAuth.activeCoachClubAffiliation!!.subCategoryName!!.isNotEmpty()) {
            owner.apply {
                subCategoryId = ManagerAuth.activeCoachClubAffiliation!!.subCategoryId!!
                subCategoryName = ManagerAuth.activeCoachClubAffiliation!!.subCategoryName!!
            }
        }

        return owner
    }


}