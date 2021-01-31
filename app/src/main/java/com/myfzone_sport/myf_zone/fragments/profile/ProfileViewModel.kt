package com.myfzone_sport.myf_zone.fragments.profile

import androidx.lifecycle.ViewModel

/**
 * Created by Amadou on 01/12/2020, 23:47
 *
 * Profile ViewModel class
 *
 */

class ProfileViewModel : ViewModel() {

    fun getCurrentUser() = ProfileService.getCurrentUser()
    fun getCurrentUserClub() = ProfileService.getUserClub()
    fun getCurrentUserEvents() = ProfileService.getUserEventList()
    fun getEventOwner(eventId: String) = ProfileService.getOwnerFromEvent(eventId)
}