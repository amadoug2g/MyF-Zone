package mfz.myfzone_sport.myf_zone.screens

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collect
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.chat.Chat


/**
 * Created by Amadou on 17/12/2020
 *
 * Main ViewModel class
 *
 */


class MainViewModel : ViewModel() {
    //region Variables
    private val _isUserSignedIn = MutableLiveData<Boolean>(false)
    val isUserSignedIn: LiveData<Boolean>
        get() = _isUserSignedIn

    private val _isUserAffiliated = MutableLiveData<Boolean>(false)
    val isUserAffiliated: LiveData<Boolean>
        get() = _isUserAffiliated
    //endregion

    init {
        _isUserSignedIn.value = checkUserSignedIn()
    }

    private fun checkUserSignedIn(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return (currentUser != null)
    }

    private fun affiliationStatus() = MainService.checkAffiliationStatus()

    fun addChatListener(onListen: (MutableList<Chat>) -> Unit) =
        MainService.addChatListener(onListen)

    suspend fun checkUserAffiliationStatus() {
        affiliationStatus().collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
//                    hideProgressBar()
                    _isUserAffiliated.value = state.data
                }
                is State.Failed -> {
                    _isUserAffiliated.value = false
                    val message =
                        "An error occurred [in checkUserAffiliationStatus]: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
//                    hideProgressBar()
                }
            }
        }
    }
}