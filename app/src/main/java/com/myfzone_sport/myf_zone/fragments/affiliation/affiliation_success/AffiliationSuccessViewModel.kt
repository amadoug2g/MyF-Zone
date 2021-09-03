package com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_success

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.coroutines.flow.collect

/**
 * Created by Amadou on 06/12/2020, 13:08
 *
 * Affiliation Success ViewModel class
 *
 */

class AffiliationSuccessViewModel : ViewModel() {

    private val _club = MutableLiveData<Club>()
    val club: LiveData<Club>
        get() = _club

    fun getUserClub() = AffiliationSuccessService.getUserClub()

    private fun getClubFromCode(affiliationCode: String) =
        AffiliationSuccessService.getClubFromCode(affiliationCode)

    suspend fun assignCodeClub(code: String) {
        getClubFromCode(code).collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
                    _club.value = state.data
                }
                is State.Failed -> {
                    val bundleTracking = bundleOf("Affiliation Success Error" to state.message)
                    Constants.TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                    val message = "An error occurred: ${state.message}"
                    Log.i("EventDetailsViewModel", message)
                }
            }
        }
    }
}