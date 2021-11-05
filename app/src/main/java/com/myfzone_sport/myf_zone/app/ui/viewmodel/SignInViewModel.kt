package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.text.TextUtils
import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.checkUserStatus
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.usecases.registration.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 18/10/2021, 22:40
 */

class SignInViewModel(
    private val signInUserUseCase: SignInUserUseCase
): ViewModel() {

    //region Variables
    val signInEmail = MutableLiveData<String>()
    val signInPassword = MutableLiveData<String>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _errorMessageEmail = MutableLiveData(false)
    val errorMessageEmail = _errorMessageEmail

    private val _errorMessagePassword = MutableLiveData(false)
    val errorMessagePassword = _errorMessagePassword

    private val _successfulSignIn = MutableLiveData(false)
    val successfulSignIn = _successfulSignIn
    //endregion

    //region Functions
    fun signIn() {
        if (validateSignInForm()) {
            signInUser(signInEmail.value!!, signInPassword.value!!)
        } else {
            onSignInResult("Fill all sign in fields!")
        }
    }

    private fun signInUser(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            signInUserUseCase.invoke(email, password).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startSignInLoading()
                    }
                    is State.Success -> {
                        signInComplete()
                        onSignInResult()
                    }
                    is State.Failed -> {
                        val message = "Sign in failure: ${state.message}"
                        onSignInResult(message)
                    }
                }
            }
        }
    }

    private fun validateSignInForm(): Boolean {
        _errorMessageEmail.value = TextUtils.isEmpty(signInEmail.value)
        _errorMessagePassword.value = TextUtils.isEmpty(signInPassword.value)

        return (!_errorMessageEmail.value!! && !_errorMessagePassword.value!!)
    }

    private fun onSignInResult(message: String = "") {
        _errorMessage.postValue(message)
        stopSignInLoading()
    }

    private fun startSignInLoading() {
        _isLoading.postValue(true)
    }

    private fun stopSignInLoading() {
        _isLoading.postValue(false)
    }

    private fun signInComplete() {
        _successfulSignIn.postValue(true)
        checkUserStatus()
    }
    //endregion
}

class SignInViewModelFactory(
    private val signInUserUseCase: SignInUserUseCase,
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            SignInUserUseCase::class.java,
        )
            .newInstance(
                signInUserUseCase
            )
    }

}