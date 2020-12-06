package mfz.myfzone_sport.myf_zone.fragments.maps.sign_up

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB

/**
 * Created by Amadou on 04/12/2020, 02:21
 *
 * Sign Up Page Service
 *
 */

object SignUpService {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun signUpUser(email: String, password: String) = flow<State<AuthResult>> {
        emit(State.loading())

        val auth = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

        emit(State.success(auth))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun addUserToDB(coach: Coach) = flow<State<Coach>> {
        val user = firebaseAuth.currentUser
        val mDataBaseQuery = DB.document(COACH_PATH + "/${user?.uid}")

        val profileUpdates = userProfileChangeRequest {
            displayName = "${coach.firstName} ${coach.lastName}"
        }

//        user?.updateProfile(profileUpdates)
//            ?.addOnCompleteListener {
//                if (it.isSuccessful) {
//                    Log.d(TAG, "User profile updated")
//                } else {
//                    Log.d(TAG, "An error occurred: ${it.exception.toString()}")
//                }
//            }


        emit(State.loading())

        mDataBaseQuery.set(coach.toMap()).await()

        emit(State.success(coach))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun assignDisplayName(coach: Coach) = flow<State<Boolean>> {
        val user = firebaseAuth.currentUser

        emit(State.loading())

        val profileUpdates = userProfileChangeRequest {
            displayName = "${coach.firstName} ${coach.lastName}"
        }

        user?.updateProfile(profileUpdates)?.await()

        emit(State.Success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)
}