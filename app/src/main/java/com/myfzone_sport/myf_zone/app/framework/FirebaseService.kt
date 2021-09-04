package com.myfzone_sport.myf_zone.app.framework

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Created by Amadou on 03/09/2021, 18:30
 *
 * Firebase Services
 *
 */

object FirebaseService {
    val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firebaseInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
}