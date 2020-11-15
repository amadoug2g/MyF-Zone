package com.example.myf_zone.util.user

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.myf_zone.model.club.AffiliationRequest
import com.example.myf_zone.model.club.Club
import com.example.myf_zone.model.coach.ClubAffiliation
import com.example.myf_zone.util.Constants.CLUB_PATH
import com.example.myf_zone.util.Constants.DB
import com.example.myf_zone.util.club.CategoryUtil.getCategoryId
import com.example.myf_zone.util.club.CategoryUtil.strGetCategoryList
import com.example.myf_zone.util.club.ClubUtil.queryClubList
import com.example.myf_zone.util.club.SportUtil.getSportId
import com.example.myf_zone.util.club.SportUtil.strGetSportList
import com.example.myf_zone.util.club.SubCategoryUtil.strGetSubCategoryList
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
        affiliationCategory: String
    ) {
        val currentUser = UserAccount.auth.currentUser!!

        CoroutineScope(IO).launch {
            val clubPartner = getClubFromCode(
                code
            )!!
            val sportID = getSportId(affiliationSport)
            val categoryID = getCategoryId(sportID, affiliationCategory)

            val clubAffiliation = ClubAffiliation().apply {
                clubId = clubPartner.id
                clubAcronym = clubPartner.acronym
                clubLogo = clubPartner.logo
                sportName = affiliationSport
                sportId = sportID
                categoryName = affiliationCategory
                categoryId = categoryID
                createDate = Date(0)
            }

            val affiliationRequest = AffiliationRequest().apply {
                coachId = currentUser.uid
                coachFullName = currentUser.displayName!!
                sportName = affiliationSport
                sportId = sportID
                categoryName = affiliationCategory
                categoryId = categoryID
                status = "validate"
            }

            sendRequestToClub(
                clubPartner.id,
                affiliationRequest
            )

            if (checkRequestStatus(affiliationRequest))
                addAffiliationUser(clubAffiliation)

        }
    }

    suspend fun populateSpinners(
        sportSpinner: Spinner,
        categorySpinner: Spinner,
        subCategorySpinner: Spinner,
        context: Context,
        textView: Int
    ) {
        populateSportSpinner(
            sportSpinner,
            context,
            textView
        )
        populateCategorySpinner(
            sportSpinner,
            categorySpinner,
            context,
            textView
        )
        populateSubCategorySpinner(
            sportSpinner,
            categorySpinner,
            subCategorySpinner,
            context,
            textView
        )
    }

    private suspend fun populateSportSpinner(
        sportSpinner: Spinner,
        context: Context,
        textView: Int
    ): Boolean {
        val sportList = strGetSportList()

        return if (!sportList.isNullOrEmpty()) {
            sportSpinner.adapter = ArrayAdapter(context, textView, sportList)
            Log.d("SportUtil", "sportList: $sportList")
            true
        } else {
            false
        }
    }

    private suspend fun populateCategorySpinner(
        sportSpinner: Spinner,
        categorySpinner: Spinner,
        context: Context,
        textView: Int
    ): Boolean {
        Log.d("SportUtil", "Enter")
        Log.d("SportUtil", "Sport name: ${sportSpinner.selectedItem}")

        val sportId = getSportId("Football")
        Log.d("SportUtil", "Sport ID: $sportId")
        val categoryList = strGetCategoryList(sportId)
        Log.d(TAG, "List: $categoryList")

        return if (!categoryList.isNullOrEmpty()) {
            Log.d(TAG, "Not null")
            categorySpinner.adapter = ArrayAdapter(context, textView, categoryList)
            true
        } else {
            Log.d(TAG, "Null")
            false
        }
    }

    private suspend fun populateSubCategorySpinner(
        sportSpinner: Spinner,
        categorySpinner: Spinner,
        subCategorySpinner: Spinner,
        context: Context,
        textView: Int
    ): Boolean {
        val sportId = getSportId(sportSpinner.selectedItem.toString())
        val categoryId = getCategoryId(sportId, categorySpinner.selectedItem.toString())
        val subCategoryList = strGetSubCategoryList(sportId, categoryId)

        return if (!subCategoryList.isNullOrEmpty()) {
            subCategorySpinner.adapter = ArrayAdapter(context, textView, subCategoryList)
            subCategorySpinner.visibility = View.VISIBLE
            true
        } else {
            subCategorySpinner.visibility = View.GONE
            false
        }
    }

    fun getClubFromCode(affiliationCode: String): Club? = runBlocking {
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