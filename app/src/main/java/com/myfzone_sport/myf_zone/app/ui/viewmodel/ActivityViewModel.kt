package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myfzone_sport.myf_zone.app.framework.FirebaseService
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.usecases.user.GetUserUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

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