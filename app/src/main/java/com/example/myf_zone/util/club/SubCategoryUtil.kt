package com.example.myf_zone.util.club

import android.util.Log
import com.example.myf_zone.model.sport.SubCategory
import com.example.myf_zone.util.Constants.DB
import com.example.myf_zone.util.Constants.SPORT_PATH
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object SubCategoryUtil {
    private val TAG = SubCategoryUtil::class.java.simpleName

    suspend fun strGetSubCategoryList(sportId: String, categoryId: String): MutableList<String> {
        val result = mutableListOf<String>()
        val list = querySubCategoryList(
            sportId,
            categoryId
        )!!
        for (item in list)
            result.add(item.name)

        return result
    }

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

    fun getSubCategoryId(sportId: String, categoryId: String, subCategory: String?): String? =
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
}