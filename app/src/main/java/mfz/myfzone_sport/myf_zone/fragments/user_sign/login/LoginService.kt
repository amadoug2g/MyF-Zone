package mfz.myfzone_sport.myf_zone.fragments.user_sign.login

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.State

/**
 * Created by Amadou on 03/12/2020, 23:33
 *
 * Login Page Service
 *
 */

object LoginService {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun signInUser(email: String, password: String) = flow<State<AuthResult>> {
        emit(State.loading())

        val auth = firebaseAuth.signInWithEmailAndPassword(email, password).await()

//        if (auth.user != null)
        emit(State.success(auth))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

}