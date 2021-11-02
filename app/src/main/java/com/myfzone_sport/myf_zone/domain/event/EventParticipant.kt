package com.myfzone_sport.myf_zone.domain.event

import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliation

data class EventParticipant(
    var clubLogo: String,
    var clubAcronym: String,
    var coachId: String,
    var coachFullname: String,
    var sportId: String,
    var sportName: String,
    var categoryId: String?,
    var categoryName: String?,
    var subCategoryId: String?,
    var subCategoryName: String?,
    var status: String
) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        "", "",
        "", "",
        ""
    )

    fun toMap(): HashMap<String, Any?> {
        val result: HashMap<String, Any?> = hashMapOf(
            "clubLogo" to clubLogo,
            "clubAcronym" to clubAcronym,
            "coachId" to coachId,
            "coachFullname" to coachFullname,
            "sportId" to sportId,
            "sportName" to sportName,
            "status" to status
        )

        if (!categoryId.isNullOrEmpty() && !categoryName.isNullOrEmpty()) {
            result["categoryId"] = categoryId
            result["categoryName"] = categoryName
        }

        if (!subCategoryId.isNullOrEmpty() && !subCategoryName.isNullOrEmpty()) {
            result["subCategoryId"] = subCategoryId
            result["subCategoryName"] = subCategoryName
        }

        return result
    }

    fun confirm(): EventParticipant {
        return EventParticipant().apply {
            clubLogo = activeCoachClubAffiliation!!.clubLogo
            clubAcronym = activeCoachClubAffiliation!!.clubAcronym
            coachId = activeCoach!!.id
            coachFullname =
                "${activeCoach!!.firstName} ${activeCoach!!.lastName}"
            sportId = activeCoachClubAffiliation!!.sportId
            sportName = activeCoachClubAffiliation!!.sportName
            if (!activeCoachClubAffiliation!!.categoryId.isNullOrEmpty()) {
                categoryId = activeCoachClubAffiliation!!.categoryId
                categoryName = activeCoachClubAffiliation!!.categoryName
                if (!activeCoachClubAffiliation!!.subCategoryId.isNullOrEmpty()) {
                    subCategoryId = activeCoachClubAffiliation!!.subCategoryId
                    subCategoryName = activeCoachClubAffiliation!!.subCategoryName
                }
            }
            status = "pending"
        }
    }
}