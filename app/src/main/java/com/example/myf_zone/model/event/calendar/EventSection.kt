package com.example.myf_zone.model.event.calendar

data class EventSection(
    private val sectionName: String,
    private val sectionList: MutableList<String>
) {
    constructor() : this("", mutableListOf())

    fun getSectionName(): String {
        return sectionName
    }

    fun getSectionList(): MutableList<String> {
        return sectionList
    }
}