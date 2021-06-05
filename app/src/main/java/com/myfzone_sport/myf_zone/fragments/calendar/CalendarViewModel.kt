package com.myfzone_sport.myf_zone.fragments.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myfzone_sport.myf_zone.model.event.Event
import com.myfzone_sport.myf_zone.model.event.calendar.EventSection

/**
 * Created by Amadou on 03/12/2020, 16:49
 *
 * Calendar ViewModel class
 *
 */

class CalendarViewModel : ViewModel() {
    private val TAG = this::class.java.simpleName

    //region Variables
    private val _isListDifferent = MutableLiveData<Boolean>(false)
    val isListDifferent: LiveData<Boolean>
        get() = _isListDifferent

    private val _isListInitialized = MutableLiveData<Boolean>(false)
    val isListInitialized: LiveData<Boolean>
        get() = _isListInitialized

    private val _eventList = MutableLiveData<MutableList<Event>>(mutableListOf())
    val eventList: LiveData<MutableList<Event>>
        get() = _eventList

    private val _calendarEventList = MutableLiveData<MutableList<Event>>(mutableListOf())
    val calendarEventList: LiveData<MutableList<Event>>
        get() = _calendarEventList
    //endregion

    init {
        _isListDifferent.value = checkCalendarList()
    }

    fun listInit() {
        _isListInitialized.value = _isListInitialized.value != true
    }

    fun getEvents() = CalendarService.getEvents()

    fun eventToCalendar(eventList: MutableList<Event>) = CalendarService.eventToCalendar(eventList)

    fun addEventListener(onListen: (MutableList<Event>) -> Unit) =
        CalendarService.addEventListener(onListen)

    fun addEventListenerCalendar(onListenCal: (MutableList<EventSection>) -> Unit) =
        CalendarService.addEventListenerCalendar(onListenCal)

    fun assignEventList(list: MutableList<Event>) {
        assignCalendarList()
        _eventList.value = list
        checkCalendarList()
    }

    private fun assignCalendarList() {
        _calendarEventList.value = _eventList.value
    }

    private fun checkCalendarList(): Boolean {
        val result = (_calendarEventList.value!! != _eventList.value!!)
        _isListDifferent.value = result
        return result
    }
}