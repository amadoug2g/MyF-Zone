package mfz.myfzone_sport.myf_zone.fragments.maps.sign_up

import androidx.lifecycle.ViewModel
import mfz.myfzone_sport.myf_zone.model.coach.Coach

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