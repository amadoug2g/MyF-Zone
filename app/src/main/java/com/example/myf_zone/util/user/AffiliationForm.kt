package com.example.myf_zone.util.user

import android.util.Log
import com.example.myf_zone.model.club.AffiliationRequest
import com.example.myf_zone.model.club.Club
import com.example.myf_zone.model.coach.ClubAffiliation
import com.example.myf_zone.util.Constants.CLUB_PATH
import com.example.myf_zone.util.Constants.DB
import com.example.myf_zone.util.club.CategoryUtil.getCategoryId
import com.example.myf_zone.util.club.ClubUtil.queryClubList
import com.example.myf_zone.util.club.SportUtil.getSportId
import com.example.myf_zone.util.club.SubCategoryUtil.getSubCategoryId
import com.example.myf_zone.util.user.UserAffiliation.addAffiliationUser
import com.example.myf_zone.util.user.UserAffiliation.checkRequestStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*

object AffiliationForm {
    private val TAG = AffiliationForm::class.java.simpleName

    fun affiliationProcess(
        code: String,
        affiliationSport: String,
        affiliationCategory: String?,
        affiliationSubCategory: String?
    ) {
        val currentUser = UserAccount.auth.currentUser!!

        CoroutineScope(IO).launch {
            val clubPartner = getClubFromCode(
                code
            )!!
            val sportID = getSportId(affiliationSport)
            val categoryID = getCategoryId(sportID, affiliationCategory)
            val subCategoryID = getSubCategoryId(sportID, categoryID ?: "", affiliationSubCategory)

            val clubAffiliation = ClubAffiliation().apply {
                clubId = clubPartner.id
                clubAcronym = clubPartner.acronym
                clubLogo = clubPartner.logo
                sportName = affiliationSport
                sportId = sportID
                if (!categoryID.isNullOrEmpty()) {
                    categoryName = affiliationCategory
                    categoryId = categoryID
                    if (!subCategoryID.isNullOrEmpty()) {
                        subCategoryId = subCategoryID
                        subCategoryName = affiliationSubCategory
                    }
                }
                createDate = Calendar.getInstance().time
            }

            val affiliationRequest = AffiliationRequest().apply {
                coachId = currentUser.uid
                coachFullName = currentUser.displayName!!
                sportName = affiliationSport
                sportId = sportID
                if (!affiliationCategory.isNullOrEmpty()) {
                    categoryName = affiliationCategory
                    categoryId = categoryID
                    if (!affiliationSubCategory.isNullOrEmpty()) {
                        subCategoryId = subCategoryID
                        subCategoryName = affiliationSubCategory
                    }
                }
                status = "validate"
            }

            sendRequestToClub(clubPartner.id, affiliationRequest)

            if (checkRequestStatus(affiliationRequest))
                addAffiliationUser(clubAffiliation)
        }
    }

    private fun getClubFromCode(affiliationCode: String): Club? = runBlocking {
        withContext(IO) {
            queryClubFromCode(
                affiliationCode
            )
        }
    }

    suspend fun queryClubFromCode(affiliationCode: String): Club? {
        val list = queryClubList()!!
        for (item in list) {
            if (item.affiliationCode == affiliationCode) {
                return item
            }
        }
        return null
    }

    fun isCodeRegistered(code: String): Boolean = runBlocking {
        withContext(IO) {
            queryIsCodeRegistered(
                code
            )
        }
    }

    fun queryIsCodeRegistered(code: String): Boolean {
        val club =
            getClubFromCode(code)
        return (club is Club)
    }

    private fun sendRequestToClub(
        clubId: String,
        affiliationRequest: AffiliationRequest,
        removeListener: Boolean = false
    ) {
        val docRef = DB.collection(CLUB_PATH)
            .document(clubId)
            .collection("AffiliationRequest")
            .add(affiliationRequest)

        if (!removeListener) {
            docRef
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

//        val uniqueId = docRef.result.addSnapshotListener{ snapshot, e ->
//            if (e != null) {
//                Log.w(TAG,"Failed: $e")
//                return@addSnapshotListener
//            }
//            if (snapshot!= null && snapshot.exists()) {
//                Log.d(TAG, "Snapshot succeeded: ${snapshot.data}")
//                if (checkRequestStatus(affiliationRequest)) {
//                    addAffiliationUser(clubAffiliation)
//                }
//            } else {
//                Log.d(TAG, "Snapshot failed?: null")
//            }
//        }
//
//        if (removeListener)
//            uniqueId.remove()
    }
}