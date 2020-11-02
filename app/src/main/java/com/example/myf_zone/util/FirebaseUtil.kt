package com.example.myf_zone.util

import android.util.Log
import com.example.myf_zone.model.coach.Coach
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseUtil {
    private val TAG = FirebaseUtil::class.java.simpleName

    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    lateinit var auth: FirebaseAuth

    val db = Firebase.firestore

    val coachPath = "Env/Staging/Coach"
    val clubPath = "Env/Staging/Club"
    val eventPath = "Env/Staging/Event"
    val sportPath = "Env/Staging/Sport"

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document(
            coachPath + "/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null")}"
        )

    fun getCurrentUser(onComplete: (Coach) -> Unit) {
        currentUserDocRef.get()
            .addOnSuccessListener {
                it.toObject(Coach::class.java)?.let { it1 -> onComplete(it1) }
                Log.d(TAG, "User retrieved")
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

    fun getEvents(): Task<QuerySnapshot> {
        val docRef = db.collection(eventPath)

        return docRef.get().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "Successfully retrieved")
            } else {
                Log.d(TAG, "An error occurred: ${it.exception.toString()}")
            }
        }
    }
}