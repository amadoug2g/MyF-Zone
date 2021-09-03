package com.myfzone_sport.myf_zone.fragments.user_sign.sign_up

import androidx.lifecycle.ViewModel
import com.myfzone_sport.myf_zone.domain.coach.Coach

/**
 * Created by Amadou on 04/12/2020, 02:06
 *
 * Sign Up ViewModel class
 *
 */

class SignUpViewModel : ViewModel() {

    fun signUpUser(email: String, password: String) = SignUpService.signUpUser(email, password)

    fun addUserToDB(coach: Coach) = SignUpService.addUserToDB(coach)

    fun assignDisplayName(coach: Coach) = SignUpService.assignDisplayName(coach)
}