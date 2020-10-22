package com.example.myf_zone.model.sport

data class Sport(
    val name: String,
    val categories: MutableList<Category>
) {
    constructor() : this(
        "",
        mutableListOf()
    )
}