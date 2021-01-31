package com.myfzone_sport.myf_zone.fragments.settings

import androidx.lifecycle.ViewModel


/**
 * Created by Amadou on 22/12/2020
 */

class SettingsViewModel : ViewModel() {
    fun signOut() = SettingsService.signOut()
}