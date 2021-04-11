package com.myfzone_sport.myf_zone.model.event.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfzone_sport.myf_zone.fragments.maps.MapsService
import com.myfzone_sport.myf_zone.model.event.EventOwner
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 08/04/2021, 22:00
 *
 * ChildRecycler ViewModel class
 *
 */

class ChildRecyclerViewModel : ViewModel() {

    private val _owner = MutableLiveData<EventOwner>()
    val owner: LiveData<EventOwner>
        get() = _owner

    private suspend fun getOwner(id: String) = MapsService.getOwnerFromEvent(id)

    fun getOwnerFromEvent(id: String) {
        viewModelScope.launch {
            _owner.value = getOwner(id)
        }
    }
}