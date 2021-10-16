package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myfzone_sport.myf_zone.usecases.user.SignOutUseCase

/**
 * Created by Amadou on 16/10/2021, 17:16
 */

class SettingsViewModel(private val signOutUseCase: SignOutUseCase) : ViewModel() {
    fun signOut() {
        signOutUseCase.invoke()
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