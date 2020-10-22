package com.example.myf_zone.model.sport

data class Category(
    val name: String,
    val subcategories: MutableList<SubCategory>
) {
    constructor() : this(
        "",
        mutableListOf()
    )
}