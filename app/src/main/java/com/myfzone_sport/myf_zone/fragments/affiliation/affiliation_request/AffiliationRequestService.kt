package com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_request

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.club.AffiliationRequest
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.sport.Category
import com.myfzone_sport.myf_zone.domain.sport.Sport
import com.myfzone_sport.myf_zone.domain.sport.SubCategory
import com.myfzone_sport.myf_zone.util.Constants.CLUB_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB
import com.myfzone_sport.myf_zone.util.Constants.SPORT_PATH
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*


/**
 * Created by Amadou on 22/12/2020
 *
 * Affiliation Request Page Service
 *
 */

object AffiliationRequestService {
    private val TAG = this::class.java.simpleName
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    //region Affiliation
    fun affiliationProcess(
        code: String,
        affiliationSport: String,
        affiliationCategory: String?,
        affiliationSubCategory: String?
    ) {
        val currentUser = firebaseAuth.currentUser!!

        CoroutineScope(IO).launch {
            val clubPartner = getClubFromCode(
                code
            )!!
            val sportID = getSportId(affiliationSport)
            val categoryID = getCategoryId(sportID, affiliationCategory)
            val subCategoryID = getSubCategoryId(sportID, categoryID ?: "", affiliationSubCategory)

            val affiliationRequest = AffiliationRequest().apply {
                coachId = currentUser.uid
                coachFullname = currentUser.displayName!!
                sportName = affiliationSport
                sportId = sportID
                if (!categoryID.isNullOrEmpty()) {
                    categoryName = affiliationCategory
                    categoryId = categoryID
                }
                if (!subCategoryID.isNullOrEmpty()) {
                    subCategoryId = subCategoryID
                    subCategoryName = affiliationSubCategory
                }
                status = "validate"
            }

/*
            val clubAffiliation = ClubAffiliation().apply {
                clubId = clubPartner.id
                clubAcronym = clubPartner.acronym
                clubLogo = clubPartner.logo
                sportName = affiliationSport
                sportId = sportID
                if (!categoryID.isNullOrEmpty()) {
                    categoryName = affiliationCategory
                    categoryId = categoryID

                }
                if (!subCategoryID.isNullOrEmpty()) {
                    subCategoryId = subCategoryID
                    subCategoryName = affiliationSubCategory
                }
                createDate = Calendar.getInstance().time
            }

            if (checkRequestStatus(affiliationRequest))
                addAffiliationUser(clubAffiliation)
*/

            sendRequestToClub(clubPartner.id, affiliationRequest.toMap())
        }
    }
    //endregion

    //region Code
    private suspend fun queryClubFromCode(affiliationCode: String): Club? {
        Log.d(TAG, "queryClubFromCode: $affiliationCode")
        val list = queryClubList()
        return if (list.isNullOrEmpty()) {
            null
        } else {
            for (item in list) {
                if (item.affiliationCode == affiliationCode) {
                    return item
                }
            }
            null
        }
    }

    fun checkCode(code: String, list: MutableList<Club>): Club? {
        return if (list.isNullOrEmpty()) {
            null
        } else {
            for (club in list) {
                if (club.affiliationCode == code) {
                    return club
                }
            }
            null
        }
    }

    private fun getClubFromCode(affiliationCode: String): Club? = runBlocking {
        Log.d(TAG, "getClubFromCode: $affiliationCode")
        withContext(IO) {
            queryClubFromCode(
                affiliationCode
            )
        }
    }
    //endregion

    //region Sport
    fun getSport() = flow<State<MutableList<Sport>>> {
        emit(State.loading())

        val mSportQuery = DB.collection(SPORT_PATH)

        val snapshot = mSportQuery.get().await()

        if (!snapshot.isEmpty) emit(State.success(snapshot.toObjects(Sport::class.java)))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    suspend fun querySportList(): MutableList<Sport>? {
        val docRef = DB.collection(SPORT_PATH)
//            .orderBy("rank")

        return try {
            val sportList = mutableListOf<Sport>()
            val documents = docRef.get().await().documents
            for (doc in documents)
                sportList.add(doc.toObject()!!)

            sportList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private fun getSportId(sport: String): String = runBlocking {
        Log.d(TAG, "sport: $sport")
        withContext(IO) {
            querySportId(sport)!!
        }
    }

    private suspend fun querySportId(sport: String): String? {
        val list = querySportList()!!
        var id: String? = null

        return try {
            for (item in list)
                if (item.name == sport)
                    id = item.id

            Log.d(TAG, "Enter [querySportId.id] $id")
            id
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    fun querySportIdFromList(sport: String, list: MutableList<Sport>): String? {
        var id: String? = null

        return try {
            for (item in list)
                if (item.name == sport)
                    id = item.id

            id
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }
    //endregion

    //region Club
    fun getClub() = flow<State<MutableList<Club>>> {
        emit(State.loading())

        val mSportQuery = DB.collection(CLUB_PATH)

        val snapshot = mSportQuery.get().await()

        if (!snapshot.isEmpty) emit(State.success(snapshot.toObjects(Club::class.java)))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    private suspend fun queryClubList(): MutableList<Club>? {
        Log.d(TAG, "inside [queryClubList]")
        val docRef = DB.collection(CLUB_PATH)
        Log.d(TAG, "[queryClubList] path: ${docRef.path}")

        return try {
            val clubList = mutableListOf<Club>()
            val documents = docRef.get().await().documents
            Log.d(TAG, "queryClubList doc: $documents")
            for (doc in documents)
                clubList.add(doc.toObject()!!)

            Log.d(TAG, "queryClubList list: $clubList")
            clubList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private fun sendRequestToClub(
        clubId: String,
        affiliationRequest: HashMap<String, Any?>,
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
/*  SnapshotListener
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
 */
    }
    //endregion

    //region Category
    fun getCategory(sport: Sport) = flow<State<MutableList<Category>>> {
        emit(State.loading())

        val mCategoryQuery = DB.collection(SPORT_PATH + "/${sport.id}/Category")

        val snapshot = mCategoryQuery.get().await()

        if (!snapshot.isEmpty)
            emit(State.success(snapshot.toObjects(Category::class.java)))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    suspend fun queryCategoryList(sportId: String): MutableList<Category>? {
        val docRef =
            DB.collection(SPORT_PATH)
                .document(sportId)
                .collection("Category")
                .orderBy("rank")

        return try {
            val categoryList = mutableListOf<Category>()
            val documents = docRef.get().await().documents
            for (doc in documents)
                categoryList.add(doc.toObject()!!)

            categoryList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private fun getCategoryId(sportId: String, category: String?): String? = runBlocking {
        withContext(IO) {
            try {
                queryCategoryId(
                    sportId,
                    category!!
                )
            } catch (e: Exception) {
                Log.d(TAG, "Error in getCategoryId", e)
                null
            }

        }
    }

    private suspend fun queryCategoryId(sportId: String, category: String): String? {
//        val list = getCategoryList(sportId)
        val list = queryCategoryList(
            sportId
        )!!
        var id: String? = null

        return try {
            for (item in list)
                if (item.name == category)
                    id = item.id

            id
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    fun queryCategoryIdFromList(category: String, list: MutableList<Category>): String? {
        var id: String? = null

        return try {
            for (item in list)
                if (item.name == category)
                    id = item.id

            id
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }
    //endregion

    //region Sub-Category
    fun getSubCategory(sport: Sport, category: Category) = flow<State<MutableList<SubCategory>>> {
        emit(State.loading())

        val mSubCategoryQuery =
            DB.collection(SPORT_PATH + "/${sport.id}/Category/${category.id}/SubCategory")

        val snapshot = mSubCategoryQuery.get().await()

        if (!snapshot.isEmpty)
            emit(State.success(snapshot.toObjects(SubCategory::class.java)))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    private fun getSubCategoryList(sportId: String, categoryId: String): MutableList<SubCategory> =
        runBlocking {
            withContext(IO) {
                querySubCategoryList(
                    sportId,
                    categoryId
                )!!
            }
        }

    suspend fun querySubCategoryList(
        sportId: String,
        categoryId: String
    ): MutableList<SubCategory>? {
        val docRef =
            DB.collection(SPORT_PATH)
                .document(sportId)
                .collection("Category")
                .document(categoryId)
                .collection("SubCategory")
                .orderBy("rank")

        return try {
            val subCategoryList = mutableListOf<SubCategory>()
            val documents = docRef.get().await().documents
            for (doc in documents)
                subCategoryList.add(doc.toObject()!!)

            subCategoryList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private fun getSubCategoryId(
        sportId: String,
        categoryId: String,
        subCategory: String?
    ): String? =
        runBlocking {
            withContext(IO) {
                try {
                    querySubCategoryId(
                        sportId,
                        categoryId,
                        subCategory!!
                    )!!
                } catch (e: Exception) {
                    Log.d(TAG, "Error in getSubCategoryId", e)
                    null
                }
            }
        }

    private fun querySubCategoryId(
        sportId: String,
        categoryId: String,
        subCategory: String
    ): String? {
        val list =
            getSubCategoryList(
                sportId,
                categoryId
            )
        var id: String? = null

        return try {
            for (item in list)
                if (item.name == subCategory)
                    id = item.id

            id
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }
    //endregion

}