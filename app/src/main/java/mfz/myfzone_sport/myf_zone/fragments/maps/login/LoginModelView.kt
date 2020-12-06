package mfz.myfzone_sport.myf_zone.fragments.maps.login

import androidx.lifecycle.ViewModel

/**
 * Created by Amadou on 03/12/2020, 23:03
 *
 * Login ViewModel class
 *
 */

class LoginModelView : ViewModel() {

    fun signInUser(email: String, password: String) = LoginService.signInUser(email, password)

}