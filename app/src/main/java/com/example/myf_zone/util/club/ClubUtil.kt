package com.example.myf_zone.util.club

import android.util.Log
import com.example.myf_zone.model.club.Club
import com.example.myf_zone.util.Constants.CLUB_PATH
import com.example.myf_zone.util.Constants.DB
import com.example.myf_zone.util.Constants.SPORT_PATH
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object ClubUtil {
    private val TAG = ClubUtil::class.java.simpleName

    suspend fun strGetClubList(): MutableList<String> {
        val result = mutableListOf<String>()
        val list = queryClubList()!!
        for (item in list)
            result.add(item.name)

        return result
    }

    fun getClubList(): MutableList<Club> = runBlocking {
        withContext(IO) {
            queryClubList()!!
        }
    }

    suspend fun queryClubList(): MutableList<Club>? {
        val docRef = DB.collection(CLUB_PATH)

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

    fun getClubId(club: String): String = runBlocking {
        withContext(IO) {
            queryClubId(club)!!
        }
    }

    private suspend fun queryClubId(club: String): String? {
        val docRef = DB.collection(SPORT_PATH).whereEqualTo("name", club)

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
}