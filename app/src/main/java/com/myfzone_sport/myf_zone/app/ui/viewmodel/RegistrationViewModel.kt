package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.text.TextUtils
import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.usecases.registration.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

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
    //region Sign Up Variables
    val signUpEmail = MutableLiveData<String>()
    val signUpPassword = MutableLiveData<String>()
    val signUpFirstName = MutableLiveData<String>()
    val signUpLastName = MutableLiveData<String>()

    private val _isSignUpLoading = MutableLiveData<Boolean>()
    val isSignUpLoading = _isSignUpLoading

    private val _errorSignUpMessage = MutableLiveData<String>()
    val errorSignUpMessage: LiveData<String> = _errorSignUpMessage

    private val _errorEmailSignUp = MutableLiveData(false)
    val errorEmailSignUp = _errorEmailSignUp

    private val _errorPasswordSignUp = MutableLiveData(false)
    val errorPasswordSignUp = _errorPasswordSignUp

    private val _errorFirstNameSignUp = MutableLiveData(false)
    val errorFirstNameSignUp = _errorFirstNameSignUp

    private val _errorLastNameSignUp = MutableLiveData(false)
    val errorLastNameSignUp = _errorLastNameSignUp

    private val _successfulSignUp = MutableLiveData(false)
    val successfulSignUp = _successfulSignUp
    //endregion

    //region Sign In Variables
    val signInEmail = MutableLiveData<String>()
    val signInPassword = MutableLiveData<String>()

    private val _isSignInLoading = MutableLiveData<Boolean>()
    val isSignInLoading = _isSignInLoading

    private val _errorSignInMessage = MutableLiveData<String>()
    val errorSignInMessage: LiveData<String> = _errorSignInMessage

    private val _errorEmailSignIn = MutableLiveData(false)
    val errorEmailSignIn = _errorEmailSignIn

    private val _errorPasswordSignIn = MutableLiveData(false)
    val errorPasswordSignIn = _errorPasswordSignIn

    private val _successfulSignIn = MutableLiveData(false)
    val successfulSignIn = _successfulSignIn
    //endregion
    //endregion

    //region Sign Up
    fun signUp() {
        if (validateSignUpForm()) {
            signUpUser(signInEmail.value!!, signInPassword.value!!)
        } else {
            onSignUpResult("Fill all sign up fields!")
        }
    }

    private fun signUpUser(email: String, password: String) {
        viewModelScope.launch(IO) {
            signUpUserUseCase.invoke(email, password).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startSignUpLoading()
                    }
                    is State.Success -> {
                        val time = Calendar.getInstance().time

                        val coach = Coach(
                            state.data.uid,
                            signUpEmail.value.toString(),
                            signUpFirstName.value.toString(),
                            signUpLastName.value.toString(), mutableListOf(), time
                        )

                        addUserToDatabase(coach)
                    }
                    is State.Failed -> {
                        val message = "Sign in failure: ${state.message}"
                        onSignUpResult(message)
                    }
                }
            }
        }
    }

    private fun validateSignUpForm(): Boolean {
        _errorEmailSignUp.value = TextUtils.isEmpty(signUpEmail.value)
        _errorPasswordSignUp.value = TextUtils.isEmpty(signUpPassword.value)
        _errorFirstNameSignUp.value = TextUtils.isEmpty(signUpFirstName.value)
        _errorLastNameSignUp.value = TextUtils.isEmpty(signUpLastName.value)

        return (!_errorEmailSignUp.value!! && !_errorPasswordSignUp.value!! && !_errorFirstNameSignUp.value!! && !_errorLastNameSignUp.value!!)
    }

    private fun addUserToDatabase(coach: Coach) {
        viewModelScope.launch(IO) {
            addUserToDatabaseUseCase.invoke(coach).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startSignUpLoading()
                    }
                    is State.Success -> {
                        assignUserName(coach)
                    }
                    is State.Failed -> {
                        val message = "Sign in failure: ${state.message}"
                        onSignUpResult(message)
                    }
                }
            }
        }
    }

    private fun assignUserName(coach: Coach) {
        viewModelScope.launch(IO) {
            assignDisplayNameUseCase.invoke(coach).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startSignUpLoading()
                    }
                    is State.Success -> {
                        signUpComplete()
                        onSignUpResult()
                    }
                    is State.Failed -> {
                        val message = "Sign in failure: ${state.message}"
                        onSignUpResult(message)
                    }
                }
            }
        }
    }

    private fun onSignUpResult(message: String = "") {
        _errorSignUpMessage.postValue(message)
        stopSignUpLoading()
    }

    private fun startSignUpLoading() {
        _isSignUpLoading.postValue(true)
    }

    private fun stopSignUpLoading() {
        _isSignUpLoading.postValue(false)
    }

    private fun signUpComplete() {
        _successfulSignUp.postValue(true)

    }
    //endregion

    //region Sign In
    fun signIn() {
        if (validateSignInForm()) {
            signInUser(signInEmail.value!!, signInPassword.value!!)
        } else {
            onSignInResult("Fill all sign in fields!")
        }
    }

    private fun signInUser(email: String, password: String) {
        viewModelScope.launch(IO) {
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
        _errorEmailSignIn.value = TextUtils.isEmpty(signInEmail.value)
        _errorPasswordSignIn.value = TextUtils.isEmpty(signInPassword.value)

        return (!_errorEmailSignIn.value!! && !_errorPasswordSignIn.value!!)
    }

    private fun onSignInResult(message: String = "") {
        _errorSignInMessage.postValue(message)
        stopSignInLoading()
    }

    private fun startSignInLoading() {
        _isSignInLoading.postValue(true)
    }

    private fun stopSignInLoading() {
        _isSignInLoading.postValue(false)
    }

    private fun signInComplete() {
        _successfulSignIn.postValue(true)
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