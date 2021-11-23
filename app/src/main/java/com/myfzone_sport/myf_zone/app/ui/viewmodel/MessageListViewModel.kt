package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.firebaseInstance
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserUseCase
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 20/11/2021, 21:49
 */

class MessageListViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase
) : ViewModel() {

    //region Variables
    private val _coach = MutableLiveData<Coach>()
    val coach: LiveData<Coach> = _coach

    private val _query = MutableLiveData<CollectionReference>()
    val query: LiveData<CollectionReference> = _query

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    fun assignCoachId(coachId: String) {
        getUser()
        _query.postValue(getQuery(coachId))
    }

    private fun getUser() {
        viewModelScope.launch {
            getUserUseCase.load().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val coach = state.data

                        _coach.postValue(coach)

                        onResult()
                    }
                    is State.Failed -> {
                        val message = "User update failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getQuery(coachId: String): CollectionReference {
        return firebaseInstance.collection("$COACH_PATH/$coachId/Chat")
    }

    fun getImageReference(path: String): StorageReference {
        return getImageReferenceUseCase.invoke(path)
    }
    //endregion

    //region Observers
    private fun onResult(message: String = "") {
        _errorMessage.postValue(message)
        stopLoading()
        resetErrorMsg()
    }

    private fun resetErrorMsg() {
        _errorMessage.postValue("")
    }

    private fun startLoading() {
        _isLoading.postValue(true)
    }

    private fun stopLoading() {
        _isLoading.postValue(false)
    }
    //endregion
}

class MessageListViewModelFactory(
    private val getUserUseCase: GetUserUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetUserUseCase::class.java,
            GetImageReferenceUseCase::class.java
        )
            .newInstance(
                getUserUseCase,
                getImageReferenceUseCase
            )
    }
}