package com.myfzone_sport.myf_zone.model.event.calendar

import com.myfzone_sport.myf_zone.model.event.EventCalendar

data class EventSection(
    private val sectionName: String,
    private val sectionList: MutableList<EventCalendar>
) {
    constructor() : this("", mutableListOf())

    fun getSectionName(): String {
        return sectionName
    }

    fun getSectionList(): MutableList<EventCalendar> {
        return sectionList
    }

    fun getText() {

    }

    override fun toString(): String {
        return "$sectionName - $sectionList"
    }
}