package com.myfzone_sport.myf_zone.domain.coach

data class EventRequest(
    val clubLogo: String,
    val clubAcronym: String,
    val coachId: String,
    val coachFullName: String,
    val sportId: String,
    val sportName: String,
    val categoryId: String,
    val categoryName: String,
    val subCategoryId: String,
    val subCategoryName: String,
    val status: String
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
        "",
        ""
    )
}