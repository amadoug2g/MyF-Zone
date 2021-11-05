package com.myfzone_sport.myf_zone.app.ui.viewmodel

import androidx.lifecycle.*
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.usecases.affiliation.GetClubIdUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetEventFromIdUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetOwnerFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 05/11/2021, 14:42
 */

class AffiliationSuccessViewModel(
    private val getClubIdUseCase: GetClubIdUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase
) : ViewModel() {

    //region Variables
    private val _club = MutableLiveData<Club>()
    val club : LiveData<Club> = _club

    private val clubId = MutableLiveData<String>()

    private val _clubImagePath = MutableLiveData<StorageReference>()
    val clubImagePath: LiveData<StorageReference> = _clubImagePath

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    fun assignClubId(id: String) {
        clubId.postValue(id)
        getClub(id)
    }

    private fun getClub(clubId: String) {
        viewModelScope.launch {
            getClubIdUseCase.invoke(clubId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val club = state.data
                        getImageReference(club.logo)

                        _club.postValue(club)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Club fetching failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getImageReference(path: String) {
        viewModelScope.launch(IO) {
            _clubImagePath.postValue(getImageReferenceUseCase.invoke(path))
        }
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

class AffiliationSuccessViewModelFactory(
    private val getClubIdUseCase: GetClubIdUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetClubIdUseCase::class.java,
            GetImageReferenceUseCase::class.java
        )
            .newInstance(
                getClubIdUseCase,
                getImageReferenceUseCase
            )
    }
}