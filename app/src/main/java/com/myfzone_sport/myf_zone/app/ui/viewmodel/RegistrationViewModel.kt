package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.usecases.registration.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 04/09/2021, 16:01
 */

class RegistrationViewModel(
    private val addUserToDatabaseUseCase: AddUserToDatabaseUseCase,
    private val assignDisplayNameUseCase: AssignDisplayNameUseCase,
    private val assignProfileImageUseCase: AssignProfileImageUseCase,
    private val signInUserUseCase: SignInUserUseCase,
    private val signUpUserUseCase: SignUpUserUseCase
) : ViewModel() {

    //region Variables
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

//    private val _isLoadingSignIn = MutableLiveData<Boolean>()
//    val isLoadingSignIn = _isLoadingSignIn
//
//    private val _isLoadingSignUp = MutableLiveData<Boolean>()
//    val isLoadingSignUp = _isLoadingSignUp

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    val signUpEmail = MutableLiveData<String>()
    val signUpPassword = MutableLiveData<String>()
    val signUpFirstName = MutableLiveData<String>()
    val signUpLastName = MutableLiveData<String>()

    val signInEmail = MutableLiveData<String>()
    val signInPassword = MutableLiveData<String>()
    //endregion

    //region Sign In
    fun signIn() {
        if (validateSignInForm()) {
            onResult("Successful SignIn !")
//            signInUser(signInEmail.value!!, signInPassword.value!!)
        } else {
            onResult("Fill all sign in fields!")
        }
    }

    private fun signInUser(email: String, password: String) {
        viewModelScope.launch(IO) {
            signInUserUseCase.invoke(email, password).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Sign in failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun validateSignInForm(): Boolean {
        return (signInEmail.value?.isNotEmpty() == true && signInPassword.value?.isNotEmpty() == true)
    }
    //endregion

    //region Sign Up
    fun signUp() {
        if (validateSignUpForm()) {
            onResult("Successful SignUp !")
            signUpUser(signInEmail.value!!, signInPassword.value!!)
        } else {
            onResult("Fill all sign up fields!")
        }
    }

    private fun signUpUser(email: String, password: String) {
        viewModelScope.launch(IO) {
            signUpUserUseCase.invoke(email, password).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Sign in failure: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun validateSignUpForm(): Boolean {
        Log.i("validation form sign up", "signUpEmail: $signUpEmail")
        Log.i("validation form sign up", "signUpPassword: $signUpPassword")
        Log.i("validation form sign up", "signUpFirstName: $signUpFirstName")
        Log.i("validation form sign up", "signUpLastName: $signUpLastName")

        return (signUpEmail.value?.isNotEmpty() == true
                && signUpPassword.value?.isNotEmpty() == true
                && signUpFirstName.value?.isNotEmpty() == true
                && signUpLastName.value?.isNotEmpty() == true)
    }
    //endregion

    //region Functions
    //endregion

    //region Observers
    private fun onResult(message: String = "") {
        _errorMessage.postValue(message)
        stopLoading()
    }

    private fun startLoading() {
        _isLoading.postValue(true)
    }

    private fun stopLoading() {
        _isLoading.postValue(false)
    }
    //endregion
}

class RegistrationViewModelFactory(
    private val addUserToDatabaseUseCase: AddUserToDatabaseUseCase,
    private val assignDisplayNameUseCase: AssignDisplayNameUseCase,
    private val assignProfileImageUseCase: AssignProfileImageUseCase,
    private val signInUserUseCase: SignInUserUseCase,
    private val signUpUserUseCase: SignUpUserUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            AddUserToDatabaseUseCase::class.java,
            AssignDisplayNameUseCase::class.java,
            AssignProfileImageUseCase::class.java,
            SignInUserUseCase::class.java,
            SignUpUserUseCase::class.java,
        )
            .newInstance(
                addUserToDatabaseUseCase,
                assignDisplayNameUseCase,
                assignProfileImageUseCase,
                signInUserUseCase,
                signUpUserUseCase
            )
    }

}