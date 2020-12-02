package mfz.myfzone_sport.myf_zone.model.event

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
}