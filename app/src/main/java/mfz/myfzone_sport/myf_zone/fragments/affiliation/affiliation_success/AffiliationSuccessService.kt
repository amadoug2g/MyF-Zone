package mfz.myfzone_sport.myf_zone.fragments.affiliation.affiliation_success

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.club.Club
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.util.Constants
import mfz.myfzone_sport.myf_zone.util.Constants.CLUB_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB

/**
 * Created by Amadou on 06/12/2020, 13:12
 *
 * Affiliation Success Page Service
 *
 */

object AffiliationSuccessService {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    fun getUserClub() = flow<State<ClubAffiliation>> {
        val userId = firebaseAuth.currentUser?.uid
        val mClubQuery = Constants.DB
            .collection(Constants.COACH_PATH + "/${userId}/ClubAffiliation")

        emit(State.loading())

        val snapshot = mClubQuery.get().await().documents[0]
        val currentUserClub = snapshot.toObject(ClubAffiliation::class.java)

        emit(State.success(currentUserClub!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun getClubFromCode(affiliationCode: String) = flow<State<Club>> {
        emit(State.loading())

        val mClubListQuery = DB.collection(CLUB_PATH)

        val snapshot = mClubListQuery.get().await().documents

        snapshot.forEach {
            val temp: Club = it.toObject()!!
            if (temp.affiliationCode == affiliationCode) {
                emit(State.success(temp))
                return@flow
            }
        }
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun getClubList() = flow<State<MutableList<Club>>> {
        emit(State.loading())

        val mClubListQuery = DB.collection(CLUB_PATH)

        val snapshot = mClubListQuery.get().await().documents
        val clubList = mutableListOf<Club>()

        snapshot.forEach { clubList.add(it.toObject()!!) }

        emit(State.success(clubList))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun getImageReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))
}