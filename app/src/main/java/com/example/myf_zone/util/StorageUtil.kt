package com.example.myf_zone.util

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.myf_zone.model.club.AffiliationRequest
import com.example.myf_zone.model.club.Club
import com.example.myf_zone.model.coach.ClubAffiliation
import com.example.myf_zone.model.event.Event
import com.example.myf_zone.model.event.EventParticipant
import com.example.myf_zone.model.sport.Category
import com.example.myf_zone.model.sport.Sport
import com.example.myf_zone.model.sport.SubCategory
import com.example.myf_zone.util.FirebaseUtil.addAffiliationUser
import com.example.myf_zone.util.FirebaseUtil.auth
import com.example.myf_zone.util.FirebaseUtil.checkRequestStatus
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

object StorageUtil {
    private val TAG = StorageUtil::class.java.simpleName

    val db = Firebase.firestore
    private val globalEventList: MutableList<Event> = mutableListOf()

    const val coachPath = "Env/Staging/Coach"
    private const val clubPath = "Env/Staging/Club"
    private const val eventPath = "Env/Staging/Event"
    private const val sportPath = "Env/Staging/Sport"

    fun getEvents(): Task<QuerySnapshot> {
        val docRef = db.collection(eventPath)

        return docRef.get().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "Successfully retrieved [getEvents()]")
            } else {
                Log.d(TAG, "An error occurred: ${it.exception.toString()}")
            }
        }
    }

    fun getEventList() {
        CoroutineScope(IO).launch {
            val x = getEventsByDate()
            for (i in x!!) {
                Log.d(TAG, "Inside Loop: ${i.owner.clubAcronym}")
            }

            Log.d(TAG, x.toString())
        }
    }

    private suspend fun getEventsByDate(): MutableList<Event>? {
        val docRef = db.collection(eventPath).orderBy("date")

        return try {
            val eventList = mutableListOf<Event>()
            val documents = docRef.get().await().documents
            for (doc in documents) {
                val owner = getOwnerFromEvent(doc.id)
                val participantList = getParticipantsFromEvent(doc.id)
                val eventToAdd: Event = doc.toObject()!!

                eventToAdd.owner = owner!!
                eventToAdd.participants = participantList!!

                eventList.add(eventToAdd)
                addItem(globalEventList, eventToAdd)
            }

            eventList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private suspend fun getOwnerFromEvent(eventId: String): EventParticipant? {
        val docRef =
            db.collection(eventPath)
                .document(eventId)
                .collection("Owner")

        return try {
            val owner = docRef.get().await().documents[0].toObject<EventParticipant>()!!
            Log.d(TAG, "Club Acronym Inside Function: ${owner.clubAcronym}")
            owner
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private suspend fun getParticipantsFromEvent(eventId: String): MutableList<EventParticipant>? {
        val docRef = db.collection(eventPath).document(eventId).collection("Participants")

        return try {
            val participationList = mutableListOf<EventParticipant>()
            val documents = docRef.get().await().documents
            for (doc in documents)
                participationList.add(doc.toObject()!!)

            participationList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private fun addItem(list: MutableList<Event>, vararg item: Event) {
        for (i in item) {
            if (!list.contains(i)) {
                list.add(i)
            }
        }
    }


    //FIREBASE QUERIES

    private suspend fun querySportList(): MutableList<Sport>? {
        val docRef = db.collection(sportPath).orderBy("rank")

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

    fun getSportList(): MutableList<Sport> = runBlocking {
        Log.d(TAG, "getSportList")
        withContext(IO) {
            querySportList()!!
        }
    }

    private suspend fun queryCategoryList(sportId: String): MutableList<Category>? {
        val docRef =
            db.collection(sportPath)
                .document(sportId)
                .collection("Category")

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

    fun getCategoryList(sportId: String): MutableList<Category> = runBlocking {
        Log.d(TAG, "getSportId")
        withContext(IO) {
            queryCategoryList(sportId)!!
        }
    }

    private suspend fun querySubCategoryList(
        sportId: String,
        categoryId: String
    ): MutableList<SubCategory>? {
        val docRef =
            db.collection(sportPath)
                .document(sportId)
                .collection("Category")
                .document(categoryId)
                .collection("SubCategory").orderBy("rank")

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

    fun getSubCategoryList(sportId: String, categoryId: String): MutableList<SubCategory> =
        runBlocking {
            Log.d(TAG, "getSportId")
            withContext(IO) {
                querySubCategoryList(sportId, categoryId)!!
            }
        }


    private suspend fun queryClubFromCode(code: String): Club {
        val clubList = getClubList()!!
        return checkCode(clubList, code)
    }

    fun getClubFromCode(code: String): Club = runBlocking {
        withContext(IO) {
            queryClubFromCode(code)
        }
    }

    private suspend fun queryCheckCodeClub(code: String): Boolean {
        val clubList = getClubList()!!
        val clubPartner = checkCode(clubList, code)

        return (clubList.contains(clubPartner))
    }

    fun checkCodeClub(code: String): Boolean = runBlocking {
        withContext(IO) {
            queryCheckCodeClub(code)
        }
    }

    fun checkAffiliationCode(
        code: String,
        affiliationSport: String,
        affiliationCategory: String
    ) {
        val currentUser = auth.currentUser!!

        CoroutineScope(IO).launch {
            val clubList = getClubList()!!
            val clubPartner = checkCode(clubList, code)

            if (clubList.contains(clubPartner)) {
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

                sendRequestToClub(clubPartner.id, affiliationRequest)

                if (checkRequestStatus(affiliationRequest))
                    addAffiliationUser(clubAffiliation)
            }
        }
    }

    private suspend fun queryClubId(club: String): String? {
        val docRef = db.collection(sportPath).whereEqualTo("name", club)

        return try {
            val id: String
            val documents = docRef.get().await().documents
            id = documents[0].id

            id
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    fun getClubId(sport: String): String = runBlocking {
        queryClubId(sport)!!
    }

    private suspend fun querySportId(sport: String): String? {
        Log.d(TAG, "querySportId")
        val docRef = db.collection(sportPath).orderBy("rank")

        return try {
            var id: String? = null
            val sportList = mutableListOf<Sport>()
            val documents = docRef.get().await().documents
            for (doc in documents)
                sportList.add(doc.toObject()!!)

            for (item in sportList) {
                if (item.name == sport)
                    id = item.id
            }

            id
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }


//        val docRef = db.collection(sportPath).orderBy("rank")
//
//        return try {
//            val sportList = mutableListOf<Sport>()
//            val documents = docRef.get().await().documents
//            for (doc in documents)
//                sportList.add(doc.toObject()!!)
//
//            sportList
//        } catch (e: Exception) {
//            Log.e(TAG, e.toString())
//            null
//        }
    }

    fun getSportId(sport: String): String = runBlocking {
        Log.d(TAG, "getSportId")
        withContext(IO) {
            querySportId(sport)!!
        }
    }

    suspend fun sportID(sport: String): String? {
        val list = querySportList()!!
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

    suspend fun categoryID(sportId: String, category: String): String? {
        Log.d(TAG, "categoryID")
        val list = queryCategoryList(sportId)!!
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

    private suspend fun sportSpinnerHandler(
        sportSpinner: Spinner,
        context: Context,
        textView: Int
    ) {
        val sportList = querySportSpinnerData()!!
        if (!sportList.isNullOrEmpty())
            populateSportSpinner(sportSpinner, context, textView, sportList)
    }

    private suspend fun categorySpinnerHandler(
        categorySpinner: Spinner,
        sportId: String,
        context: Context,
        textView: Int
    ) {
        val categoryList = queryCategorySpinnerData(sportId)!!
        if (!categoryList.isNullOrEmpty())
            populateCategorySpinner(categorySpinner, context, textView, categoryList)
    }

    suspend fun subCategorySpinnerHandler(
        subCategorySpinner: Spinner,
        sportId: String,
        categoryId: String,
        context: Context,
        textView: Int
    ): Boolean {
        val subCategoryList = querySubCategorySpinnerData(sportId, categoryId)!!
        return if (!subCategoryList.isNullOrEmpty()) {
            populateSubCategorySpinner(subCategorySpinner, context, textView, subCategoryList)
            true
        } else {
            subCategorySpinner.visibility = View.GONE
            false
        }
    }


    suspend fun populateSpinners(
        sportSpinner: Spinner,
        categorySpinner: Spinner,
        subCategorySpinner: Spinner,
        context: Context,
        textView: Int
    ) {
        sportSpinnerHandler(sportSpinner, context, textView)

        val sportId = sportID(sportSpinner.selectedItem.toString())!!

        categorySpinnerHandler(categorySpinner, sportId, context, textView)

        val categoryId = categoryID(sportId, categorySpinner.selectedItem.toString())!!

        subCategorySpinnerHandler(subCategorySpinner, sportId, categoryId, context, textView)
    }

    private fun populateSportSpinner(
        sportSpinner: Spinner,
        context: Context,
        textView: Int,
        sportList: MutableList<String>
    ) {
        val sportAdapter = ArrayAdapter(context, textView, sportList)
        sportSpinner.adapter = sportAdapter
        Log.d(TAG, "sportSpinner populated")
    }

    private suspend fun querySportSpinnerData(): MutableList<String>? {
        val sportList = mutableListOf<String>()

        return try {
            for (sport in querySportList()!!)
                sportList.add(sport.name)

            sportList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private fun populateCategorySpinner(
        categorySpinner: Spinner,
        context: Context,
        textView: Int,
        categoryList: MutableList<String>
    ) {
        val categoryAdapter = ArrayAdapter(context, textView, categoryList)
        categorySpinner.adapter = categoryAdapter
        Log.d(TAG, "categorySpinner populated")
    }

    private suspend fun queryCategorySpinnerData(sportId: String): MutableList<String>? {
        val categoryList = mutableListOf<String>()

        return try {
            for (sport in queryCategoryList(sportId)!!)
                categoryList.add(sport.name)

            categoryList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    private fun populateSubCategorySpinner(
        subCategorySpinner: Spinner,
        context: Context,
        textView: Int,
        subCategoryList: MutableList<String>
    ): Boolean {
        Log.d(TAG, "populateSubCategorySpinner started")
        val subCategoryAdapter = ArrayAdapter(context, textView, subCategoryList)
        var result = false
        if (!subCategoryList.isNullOrEmpty()) {
            subCategorySpinner.adapter = subCategoryAdapter
            result = true
        }
        Log.d(TAG, "subCategorySpinner populated")
        return result
    }

    private suspend fun querySubCategorySpinnerData(
        sportId: String,
        categoryId: String
    ): MutableList<String>? {
        Log.d(TAG, "querySubCategorySpinnerData started")
        val subCategoryList = mutableListOf<String>()

        return try {
            for (sport in querySubCategoryList(sportId, categoryId)!!)
                subCategoryList.add(sport.name)

            subCategoryList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }


    private suspend fun queryCategoryId(sportId: String, category: String): String? {
        Log.d(TAG, "queryCategoryId")
        val docRef =
            db.collection(sportPath)
                .document(sportId)
                .collection("Category")
                .whereEqualTo("name", category)

        return try {
            val id: String
            val documents = docRef.get().await().documents
            Log.d(TAG, "Category documents are $documents")
            Log.d(TAG, "Sport Id $sportId")
            Log.d(TAG, "Category is $category")
            id = documents[0].id

            id
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    fun getCategoryId(sport: String, category: String): String = runBlocking {
        Log.d(TAG, "getCategoryId")
        withContext(IO) {
            queryCategoryId(sport, category)!!
        }
    }

    private suspend fun getClubList(): MutableList<Club>? {
        val docRef = db.collection(clubPath)

        return try {
            val clubList = mutableListOf<Club>()
            val documents = docRef.get().await().documents
            for (doc in documents)
                clubList.add(doc.toObject()!!)

            clubList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    val clubList: MutableList<Club> = runBlocking {
        getClubList()!!
    }

    val eventList: MutableList<Event> = runBlocking {
        getEventsByDate()!!
    }

    private fun checkCode(list: MutableList<Club>, affiliationCode: String): Club {
        for (item in list) {
            if (item.affiliationCode == affiliationCode) {
                return item
            }
        }
        return Club()
    }

    fun getClubAffiliationCode() {}

    private fun sendRequestToClub(
        clubId: String,
        affiliationRequest: AffiliationRequest,
        removeListener: Boolean = false
    ) {
        val docRef = db.collection(clubPath)
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

    private fun stampToDate(time: Timestamp): Date {
        return time.toDate()
    }
}