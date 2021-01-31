package com.myfzone_sport.myf_zone.fragments.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by Amadou on 02/12/2020, 14:53
 *
 * : used to instantiate ProfileViewModel
 *
 */

class ProfileViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel() as T
        }
        throw IllegalAccessException("Unknown ViewModel class")
    }
}