package com.myfzone_sport.myf_zone.domain.sport

data class SubCategory(
    var id: String,
    var name: String,
    var rank: Int
) {
    constructor() : this("", "", 0)
}