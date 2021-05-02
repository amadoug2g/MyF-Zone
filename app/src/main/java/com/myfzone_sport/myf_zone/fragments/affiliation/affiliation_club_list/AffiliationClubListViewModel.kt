package com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_club_list

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.CollectionReference
import com.myfzone_sport.myf_zone.util.Constants.CLUB_PATH

/**
 * Created by Amadou on 30/04/2021, 22:05
 *
 * Affiliation Club List ViewModel class
 *
 */

class AffiliationClubListViewModel : ViewModel() {
    fun getQuery(): CollectionReference {
        return AffiliationClubListService.fireStoreInstance
            .collection(CLUB_PATH)
    }
}