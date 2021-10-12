package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myfzone_sport.myf_zone.app.framework.FirebaseService
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.usecases.user.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ActivityViewModel(
    private val getUserStatusUseCase: GetUserStatusUseCase
) : ViewModel() {

    var isUserConnected = MutableLiveData(false)

    init {
        getUserStatus()
    }

    fun getUserStatus() {
        isUserConnected.postValue(getUserStatusUseCase.invoke())
    }
}

class ActivityViewModelFactory(
    private val getUserStatusUseCase: GetUserStatusUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetUserStatusUseCase::class.java
        )
            .newInstance(
                getUserStatusUseCase
            )
    }

}