package mfz.myfzone_sport.myf_zone.fragments.message

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.chat.Chat
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.currentUserDocRef

/**
 * Created by Amadou on 07/12/2020, 01:04
 *
 * Message Page Service
 *
 */

object MessageService {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun getCurrentUser() = flow<State<Coach>> {
        val userId = firebaseAuth.currentUser?.uid
        val mUserQuery = DB.document(COACH_PATH + "/${userId}")

        emit(State.loading())

        val snapshot = mUserQuery.get().await()
        val currentUser = snapshot.toObject(Coach::class.java)

        emit(State.success(currentUser!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun getChatCoach(coachId: String) = flow<State<Chat>> {
        val userId = firebaseAuth.currentUser?.uid
        val mUserQuery = DB
            .document(COACH_PATH + "${userId}/Chat/${coachId}")

        emit(State.loading())

        val snapshot = mUserQuery.get().await()
        val coachChat = snapshot.toObject(Chat::class.java)

        emit(State.success(coachChat!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun checkAffiliationStatus() = flow<State<Boolean>> {
        val userId = firebaseAuth.currentUser?.uid
        val mAffiliationPath = DB
            .collection(COACH_PATH + "/${userId}/ClubAffiliation")

        emit(State.loading())

        val snapshot = mAffiliationPath.get().await()
        val status = snapshot.documents.size > 0

        emit(State.success(status))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    //region FCM
    fun getFCMRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject(Coach::class.java)!!
            onComplete(user.devices)
        }
    }

    fun setFCMRegistrationTokens(registrationTokens: MutableList<String>) {
        currentUserDocRef.update(mapOf("devices" to registrationTokens))
    }
    //endregion FCM

    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}