package com.example.myf_zone.util.club

import android.util.Log
import com.example.myf_zone.model.sport.Category
import com.example.myf_zone.util.Constants.DB
import com.example.myf_zone.util.Constants.SPORT_PATH
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object CategoryUtil {
    private val TAG = CategoryUtil::class.java.simpleName

    suspend fun strGetCategoryList(sportId: String): MutableList<String> {
        val result = mutableListOf<String>()
        Log.d("SportUtil", "Sport ID strGetCategoryList: $sportId")
        val list = queryCategoryList(
            sportId
        )!!
        for (item in list)
            result.add(item.name)

        return result
    }

    fun getCategoryList(sportId: String): MutableList<Category> = runBlocking {
        withContext(IO) {
            queryCategoryList(sportId)!!
        }
    }

    suspend fun queryCategoryList(sportId: String): MutableList<Category>? {
        val docRef =
            DB.collection(SPORT_PATH)
                .document(sportId)
                .collection("Category")
//                .orderBy("rank")

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

    fun getCategoryId(sportId: String, category: String?): String? = runBlocking {
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
}