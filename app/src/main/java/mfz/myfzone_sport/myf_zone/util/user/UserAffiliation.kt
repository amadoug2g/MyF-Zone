package mfz.myfzone_sport.myf_zone.util.user

import android.util.Log
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.club.AffiliationRequest
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.auth
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.currentUserDocRef

object UserAffiliation {
    private val TAG = UserAffiliation::class.java.simpleName

    fun addAffiliationUser(affiliation: HashMap<String, Any?>) {
        Log.d("AffiliationForm", "affiliation hmap! $affiliation")
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

    fun fieldToClubAffiliation(clubAffiliation: ClubAffiliation): HashMap<String, Any?> {
        val result: HashMap<String, Any?> = hashMapOf(
            "clubId" to clubAffiliation.clubId,
            "clubAcronym" to clubAffiliation.clubAcronym,
            "clubLogo" to clubAffiliation.clubLogo,
            "sportId" to clubAffiliation.sportId,
            "sportName" to clubAffiliation.sportName,
            "createDate" to clubAffiliation.createDate
        )

        if (!clubAffiliation.categoryId.isNullOrEmpty() && !clubAffiliation.categoryName.isNullOrEmpty()) {
            result["categoryId"] = clubAffiliation.categoryId
            result["categoryName"] = clubAffiliation.categoryName
        }

        if (!clubAffiliation.subCategoryId.isNullOrEmpty() && !clubAffiliation.subCategoryName.isNullOrEmpty()) {
            result["subCategoryId"] = clubAffiliation.subCategoryId
            result["subCategoryName"] = clubAffiliation.subCategoryName
        }

        return result
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
            Log.d(TAG, "$e")
            false
        }
    }

    fun updateAffiliationStatus() {}

    fun checkRequestStatus(affiliationRequest: AffiliationRequest): Boolean {
        return (affiliationRequest.status == "validate")
    }

}