package com.example.myf_zone.model.coach

import com.example.myf_zone.model.team.Team
import java.util.*

data class ClubAffiliation(
    val clubId: String,
    val clubAcronym: String,
    val clubLogo: String,
    val sportId: String,
    val sportName: String,
    val categoryId: String,
    val categoryName: String,
    val subCategoryId: String,
    val subCategoryName: String,
    val createDate: Date,
    val events: MutableList<CoachEvent>,
    val teams: MutableList<Team>
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
        Date(0),
        mutableListOf(),
        mutableListOf()
    )
}