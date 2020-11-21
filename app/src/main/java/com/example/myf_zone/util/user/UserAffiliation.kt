package com.example.myf_zone.util.user

import android.util.Log
import com.example.myf_zone.model.club.AffiliationRequest
import com.example.myf_zone.model.coach.ClubAffiliation
import com.example.myf_zone.util.Constants.COACH_PATH
import com.example.myf_zone.util.Constants.DB
import com.example.myf_zone.util.user.UserAccount.auth
import com.example.myf_zone.util.user.UserAccount.currentUserDocRef
import kotlinx.coroutines.tasks.await

object UserAffiliation {
    private val TAG = UserAffiliation::class.java.simpleName

    fun addAffiliationUser(affiliation: ClubAffiliation) {
        Log.d(TAG, "Adding ${affiliation.categoryName} as affiliation")
        val currentUser = auth.currentUser

        DB.collection(COACH_PATH)
            .document(currentUser!!.uid)
            .collection("ClubAffiliation")
            .add(affiliation)
            .addOnSuccessListener {
                Log.d(TAG, "Club affiliation added successfully")
            }
            .addOnFailureListener {
                Log.d(TAG, "Club affiliation added failed")
            }
            .addOnCompleteListener {
                Log.d(TAG, "Club affiliation added completed")
            }
    }

    fun userAffiliationStatus(myCallback: (Boolean) -> Unit) {
        val affiliationPath = currentUserDocRef.collection("ClubAffiliation")

        affiliationPath.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val documents = it.result.documents

                val result = documents.size > 0

                myCallback(result)
            }
        }
    }

    suspend fun affiliationStatus(): Boolean? {
        val affiliationPath = currentUserDocRef.collection("ClubAffiliation")

        return try {
            affiliationPath.get().await().documents.size > 0
        } catch (e: Exception) {
            Log.d(TAG, "e")
            false
        }
    }

    fun updateAffiliationStatus() {}

    fun checkRequestStatus(affiliationRequest: AffiliationRequest): Boolean {
        return (affiliationRequest.status == "validate")
    }

}