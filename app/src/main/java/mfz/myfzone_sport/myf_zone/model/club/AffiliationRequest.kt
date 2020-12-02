package mfz.myfzone_sport.myf_zone.model.club

import java.util.*

data class AffiliationRequest(
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
        "",
        "",
        ""
    )

    fun toMap(): HashMap<String, Any?> {
        val result: HashMap<String, Any?> = hashMapOf(
            "coachId" to coachId,
            "coachFullName" to coachFullname,
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
}