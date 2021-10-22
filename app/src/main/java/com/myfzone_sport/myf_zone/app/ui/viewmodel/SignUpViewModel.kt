package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.text.TextUtils
import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.usecases.registration.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

/**
 * Created by Amadou on 18/10/2021, 22:40
 */

class SignUpViewModel(
    private val addUserToDatabaseUseCase: AddUserToDatabaseUseCase,
    private val assignDisplayNameUseCase: AssignDisplayNameUseCase,
    private val assignProfileImageUseCase: AssignProfileImageUseCase,
    private val signUpUserUseCase: SignUpUserUseCase
): ViewModel() {

    //region Variables
    val signUpEmail = MutableLiveData<String>()
    val signUpPassword = MutableLiveData<String>()
    val signUpFirstName = MutableLiveData<String>()
    val signUpLastName = MutableLiveData<String>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _errorMessageEmail = MutableLiveData(false)
    val errorMessageEmail = _errorMessageEmail

    private val _errorMessagePassword = MutableLiveData(false)
    val errorMessagePassword = _errorMessagePassword

    private val _errorMessageFirstName = MutableLiveData(false)
    val errorMessageFirstName = _errorMessageFirstName

    private val _errorMessageLastName = MutableLiveData(false)
    val errorMessageLastName = _errorMessageLastName

    private val _successfulSignUp = MutableLiveData(false)
    val successfulSignUp = _successfulSignUp
    //endregion

    //region Function
    fun signUp() {
        if (validateSignUpForm()) {
            signUpUser(signUpEmail.value!!, signUpPassword.value!!)
        } else {
            onSignUpResult("Fill all sign up fields!")
        }
    }

    private fun signUpUser(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
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
        _errorMessageEmail.value = TextUtils.isEmpty(signUpEmail.value)
        _errorMessagePassword.value = TextUtils.isEmpty(signUpPassword.value)
        _errorMessageFirstName.value = TextUtils.isEmpty(signUpFirstName.value)
        _errorMessageLastName.value = TextUtils.isEmpty(signUpLastName.value)

        return (!_errorMessageEmail.value!! && !_errorMessagePassword.value!! && !_errorMessageFirstName.value!! && !_errorMessageLastName.value!!)
    }

    private fun addUserToDatabase(coach: Coach) {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
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
        _errorMessage.postValue(message)
        stopSignUpLoading()
    }

    private fun startSignUpLoading() {
        _isLoading.postValue(true)
    }

    private fun stopSignUpLoading() {
        _isLoading.postValue(false)
    }

    private fun signUpComplete() {
        _successfulSignUp.postValue(true)

    }
    //endregion
}

class SignUpViewModelFactory(
    private val addUserToDatabaseUseCase: AddUserToDatabaseUseCase,
    private val assignDisplayNameUseCase: AssignDisplayNameUseCase,
    private val assignProfileImageUseCase: AssignProfileImageUseCase,
    private val signUpUserUseCase: SignUpUserUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            AddUserToDatabaseUseCase::class.java,
            AssignDisplayNameUseCase::class.java,
            AssignProfileImageUseCase::class.java,
            SignUpUserUseCase::class.java,
        )
            .newInstance(
                addUserToDatabaseUseCase,
                assignDisplayNameUseCase,
                assignProfileImageUseCase,
                signUpUserUseCase
            )
    }

}