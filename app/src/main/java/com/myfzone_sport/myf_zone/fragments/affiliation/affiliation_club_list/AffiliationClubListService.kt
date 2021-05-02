package com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_club_list

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Created by Amadou on 30/04/2021, 22:02
 *
 * Affiliation Club List Page Service
 *
 */

object AffiliationClubListService {

    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}