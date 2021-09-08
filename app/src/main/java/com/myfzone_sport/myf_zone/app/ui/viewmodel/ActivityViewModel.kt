package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myfzone_sport.myf_zone.usecases.event.GetAllEventsUseCase
import com.myfzone_sport.myf_zone.usecases.event.GetFriendlyEventsUseCase
import com.myfzone_sport.myf_zone.usecases.event.GetPlateauEventsUseCase
import com.myfzone_sport.myf_zone.usecases.event.GetTourneyEventsUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserUseCase
import com.myfzone_sport.myf_zone.usecases.user.SignOutUseCase

class ActivityViewModel(
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    var isUserConnected = MutableLiveData(false)

    init {
        getUser()
    }

    fun getUser(): Boolean {
        isUserConnected.postValue(getUserUseCase.invoke())

        return isUserConnected.value!!
    }
}

class ActivityViewModelFactory(
    private val getUserUseCase: GetUserUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetUserUseCase::class.java
        )
            .newInstance(
                getUserUseCase
            )
    }

}