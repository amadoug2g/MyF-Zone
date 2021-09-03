package com.myfzone_sport.myf_zone.fragments.user_sign.sign_up

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.chat.MessagingService
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

/**
 * Created by Amadou on 04/12/2020, 02:21
 *
 * Sign Up Page Service
 *
 */

object SignUpService {
    private val TAG = SignUpService::class.java.simpleName
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

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d(TAG, token)

            MessagingService.addTokenToFireStore(token)
        })

        emit(State.Success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)
}