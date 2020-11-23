package mfz.myfzone_sport.myf_zone.util.club

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import mfz.myfzone_sport.myf_zone.model.sport.Sport
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.SPORT_PATH

object SportUtil {
    private val TAG = SportUtil::class.java.simpleName

    suspend fun strGetSportList(): MutableList<String> {
        val result = mutableListOf<String>()
        val list = querySportList()!!
        for (item in list)
            result.add(item.name)

        return result
    }

    fun getSportList(): MutableList<Sport> = runBlocking {
        withContext(IO) {
            querySportList()!!
        }
    }

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

    fun getSportId(sport: String): String = runBlocking {
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
}