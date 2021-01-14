package mfz.myfzone_sport.myf_zone.screens

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.chat.Chat
import mfz.myfzone_sport.myf_zone.util.Constants
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB


/**
 * Created by Amadou on 17/12/2020
 */

object MainService {
    private val TAG = this::class.java.simpleName
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun checkAffiliationStatus() = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid
        val mAffiliationPath = Constants.DB
            .collection(Constants.COACH_PATH + "/${userId}/ClubAffiliation")

        emit(State.loading())

        val snapshot = mAffiliationPath.get().await()
        val status = snapshot.documents.size > 0

        emit(State.success(status))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun addChatListener(
        onListen: (MutableList<Chat>) -> Unit
    ): ListenerRegistration? {
        val userId = firebaseAuth.currentUser?.uid
        val mUserChatQuery = DB
            .collection(COACH_PATH + "/${userId}/Chat")

        return try {
            mUserChatQuery
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "Error in addChatListener", error)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Chat>()
                    value?.documents?.forEach {

                        try {
                            items.add(it.toObject(Chat::class.java)!!)
                        } catch (e: Exception) {
                            Log.i(TAG, "Error when fetching chats: $e")
                        }
                    }

                    onListen(items)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in addChatListener: ${e.localizedMessage}")
            null
        }
    }
}