package com.example.myf_zone.model.club

data class AffiliationRequest(
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
        ""
    )
}