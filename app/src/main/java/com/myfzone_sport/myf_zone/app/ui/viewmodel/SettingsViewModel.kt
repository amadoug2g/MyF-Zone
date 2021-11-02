package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.usecases.user.SignOutUseCase
import com.myfzone_sport.myf_zone.util.Tracking.LOGOUT

/**
 * Created by Amadou on 16/10/2021, 17:16
 */

class SettingsViewModel(private val signOutUseCase: SignOutUseCase) : ViewModel() {

    private val _successfulSignOut = MutableLiveData(false)
    val successfulSignOut = _successfulSignOut

    fun signOut() {
        signOutUseCase.invoke()
        _successfulSignOut.postValue(true)
        TRACKING.logEvent(LOGOUT, null)
    }
}

class SettingsViewModelViewModelFactory(
    private val signOutUseCase: SignOutUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            SignOutUseCase::class.java
        )
            .newInstance(
                signOutUseCase
            )
    }
}