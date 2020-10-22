package com.example.myf_zone.model.event

data class EventParticipation(
    val clubLogo: String,
    val clubAcronym: String,
    val coachId: String,
    val coachFullName: String,
    val sportId: String,
    val sportName: String,
    val categoryId: String,
    val categoryName: String,
    val subCategoryId: String,
    val subCategoryName: String
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
}