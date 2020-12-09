package mfz.myfzone_sport.myf_zone.util.user

import android.util.Log
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.club.AffiliationRequest
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.currentUserDocRef

object UserAffiliation {
    private val TAG = UserAffiliation::class.java.simpleName


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
            Log.d(TAG, "$e")
            false
        }
    }

    fun updateAffiliationStatus() {}

    fun checkRequestStatus(affiliationRequest: AffiliationRequest): Boolean {
        return (affiliationRequest.status == "validate")
    }

}