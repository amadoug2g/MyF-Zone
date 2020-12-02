package mfz.myfzone_sport.myf_zone.model.event

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
}