package mfz.myfzone_sport.myf_zone.model.coach

import java.util.*

data class ClubAffiliation(
    var clubId: String,
    var clubAcronym: String,
    var clubLogo: String,
    var sportId: String,
    var sportName: String,
    var categoryId: String?,
    var categoryName: String?,
    var subCategoryId: String?,
    var subCategoryName: String?,
    var createDate: Date
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
        Date(0)
    )
}