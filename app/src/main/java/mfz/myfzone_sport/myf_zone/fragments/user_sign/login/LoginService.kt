package mfz.myfzone_sport.myf_zone.fragments.user_sign.login

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.fragments.user_sign.sign_up.SignUpService
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.chat.MessagingService

/**
 * Created by Amadou on 03/12/2020, 23:33
 *
 * Login Page Service
 *
 */

object LoginService {
    private val TAG = SignUpService::class.java.simpleName
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun signInUser(email: String, password: String) = flow<State<AuthResult>> {
        emit(State.loading())

        val auth = firebaseAuth.signInWithEmailAndPassword(email, password).await()

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

        emit(State.success(auth))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

}