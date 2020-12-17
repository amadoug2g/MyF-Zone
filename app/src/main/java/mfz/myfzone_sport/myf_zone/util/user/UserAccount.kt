package mfz.myfzone_sport.myf_zone.util.user

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH


object UserAccount {
    private val TAG = UserAccount::class.java.simpleName

    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    lateinit var auth: FirebaseAuth

    val currentUserDocRef: DocumentReference
        get() = firestoreInstance
            .document(
                COACH_PATH + "/${
                    FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception(
                        "You are not connected"
                    )
                }"
            )

    fun getCurrentUser(onComplete: (Coach) -> Unit) {
        currentUserDocRef.get()
            .addOnSuccessListener {
                it.toObject<Coach>()?.let { it1 -> onComplete(it1) }
//                Log.d(TAG, "User retrieved")
            }
    }

    fun updateCurrentUser(
        mail: String = "",
        firstName: String = "",
        lastName: String = ""
    ) {
        val coachFields = mutableMapOf<String, Any>()
        if (mail.isNotBlank()) coachFields[mail] = mail
        if (firstName.isNotBlank()) coachFields[firstName] = firstName
        if (lastName.isNotBlank()) coachFields[lastName] = lastName

        if (firstName.isNotBlank() || lastName.isNotBlank()) {
            val profileUpdates = userProfileChangeRequest {
                displayName = "$firstName $lastName"
            }

            auth.currentUser!!.updateProfile(profileUpdates)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "User profile updated")
                    } else {
                        Log.d(TAG, "An error occurred: ${it.exception.toString()}")
                    }
                }
        }
        currentUserDocRef.update(coachFields)
    }

}