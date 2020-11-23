package mfz.myfzone_sport.myf_zone.model.sport

data class Category(
    var id: String,
    var name: String,
    var rank: Int
) {
    constructor() : this(
        "", "", 0
    )
}