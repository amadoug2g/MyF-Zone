package mfz.myfzone_sport.myf_zone.fragments.event.event_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by Amadou on 02/12/2020, 21:17
 *
 * Event Edit ViewModelFactory class
 *
 */

class EventEditViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventEditViewModel::class.java)) {
            return EventEditViewModel() as T
        }
        throw IllegalAccessException("Unknown ViewModel class")
    }
}