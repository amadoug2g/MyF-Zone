package mfz.myfzone_sport.myf_zone.util.user

import android.util.Log
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.club.AffiliationRequest
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.auth
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.currentUserDocRef
import java.util.*

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


    fun addUserToDB(coach: Coach, id: String) {
//        val user = fieldToCoach(coach)
//
//        DB.collection(COACH_PATH)
//            .document(id)
//            .set(user, SetOptions.merge())
//            .addOnSuccessListener {
//                Log.d(UserAccount.TAG, "Document added successfully")
//            }
//            .addOnFailureListener {
//                Log.d(UserAccount.TAG, "Document added failed")
//            }
//            .addOnCompleteListener {
//                Log.d(UserAccount.TAG, "Document added completed")
//            }
    }

    private fun fieldToClubAffiliationComplete(clubAffiliation: ClubAffiliation): HashMap<String, Any?> {
        return hashMapOf(
            "clubAcronym" to clubAffiliation.clubAcronym,
            "clubLogo" to clubAffiliation.clubLogo,
            "sportId" to clubAffiliation.sportId,
            "sportName" to clubAffiliation.sportName,
            "categoryId" to clubAffiliation.categoryId,
            "categoryName" to clubAffiliation.categoryName,
            "subCategoryId" to clubAffiliation.subCategoryId,
            "subCategoryName" to clubAffiliation.subCategoryName,
            "createDate" to clubAffiliation.createDate
        )
    }

    private fun fieldToClubAffiliationNoCategory(clubAffiliation: ClubAffiliation): HashMap<String, Any?> {
        return hashMapOf(
            "clubAcronym" to clubAffiliation.clubAcronym,
            "clubLogo" to clubAffiliation.clubLogo,
            "sportId" to clubAffiliation.sportId,
            "sportName" to clubAffiliation.sportName,
            "createDate" to clubAffiliation.createDate
        )
    }

    private fun fieldToClubAffiliationNoSubCategory(clubAffiliation: ClubAffiliation): HashMap<String, Any?> {
        return hashMapOf(
            "clubAcronym" to clubAffiliation.clubAcronym,
            "clubLogo" to clubAffiliation.clubLogo,
            "sportId" to clubAffiliation.sportId,
            "sportName" to clubAffiliation.sportName,
            "categoryId" to clubAffiliation.categoryId,
            "categoryName" to clubAffiliation.categoryName,
            "createDate" to clubAffiliation.createDate
        )
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