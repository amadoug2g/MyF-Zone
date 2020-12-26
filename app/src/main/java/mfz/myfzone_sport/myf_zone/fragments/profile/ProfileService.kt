package mfz.myfzone_sport.myf_zone.fragments.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.EventOwner
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import mfz.myfzone_sport.myf_zone.util.Constants.EVENT_PATH

/**
 * Created by Amadou on 02/12/2020, 14:25
 *
 * Profile Page Service
 *
 */

object ProfileService {
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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

    fun getUserClub() = flow<State<ClubAffiliation>> {
        val userId = firebaseAuth.currentUser?.uid
        val mClubQuery = DB
            .collection(COACH_PATH + "/${userId}/ClubAffiliation")

        emit(State.loading())

        val snapshot = mClubQuery.get().await().documents[0]
        val currentUserClub = snapshot.toObject(ClubAffiliation::class.java)

        emit(State.success(currentUserClub!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun getUserEventList() = flow<State<MutableList<Event>>> {
        val userId = firebaseAuth.currentUser?.uid
        val mClubQuery = DB
            .collection(COACH_PATH + "/${userId}/ClubAffiliation")

        emit(State.loading())

        val clubAffiliationSnapshot = mClubQuery.get().await().documents[0]
        val currentUserClub = clubAffiliationSnapshot.toObject(ClubAffiliation::class.java)

        val clubId = currentUserClub?.clubId

        val mUserEventQuery = DB
            .collection(COACH_PATH + "/${userId}/ClubAffiliation/${clubId}/CoachEvent")

        val snapshot = mUserEventQuery.get().await().documents
        val userEventList = mutableListOf<Event>()

        snapshot.forEach { userEventList.add(it.toObject()!!) }
        userEventList.sortBy { it.date }

        emit(State.success(userEventList))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun getOwnerFromEvent(eventId: String) = flow<State<EventOwner>> {
        val mEventOwnerQuery = DB
            .collection(EVENT_PATH + "/${eventId}/Owner")

        emit(State.loading())

        val snapshot = mEventOwnerQuery.get().await().documents[0]
        val eventOwner = snapshot.toObject(EventOwner::class.java)

        emit(State.success(eventOwner!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}