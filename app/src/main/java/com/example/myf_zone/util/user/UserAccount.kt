package com.example.myf_zone.util.user

import android.util.Log
import com.example.myf_zone.model.coach.ClubAffiliation
import com.example.myf_zone.model.coach.Coach
import com.example.myf_zone.util.Constants.COACH_PATH
import com.example.myf_zone.util.Constants.DB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.util.*

object UserAccount {
    private val TAG = UserAccount::class.java.simpleName

    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    lateinit var auth: FirebaseAuth

    val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document(
            COACH_PATH + "/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null")}"
        )

    fun getCurrentUser(onComplete: (Coach) -> Unit) {
        currentUserDocRef.get()
            .addOnSuccessListener {
                it.toObject<Coach>()?.let { it1 -> onComplete(it1) }
//                Log.d(TAG, "User retrieved")
            }
    }

    fun getCurrentClub(onComplete: (ClubAffiliation) -> Unit) {
        currentUserDocRef.collection("ClubAffiliation").get()
            .addOnSuccessListener {
                val club = it.documents[0]
                club.toObject<ClubAffiliation>()?.let { it1 -> onComplete(it1) }
//                Log.d(TAG, "Club Affiliation retrieved")
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

    fun addUserToDB(coach: Coach, id: String) {
        val user = fieldToCoach(coach)

        DB.collection(COACH_PATH)
            .document(id)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "Document added successfully")
            }
            .addOnFailureListener {
                Log.d(TAG, "Document added failed")
            }
            .addOnCompleteListener {
                Log.d(TAG, "Document added completed")
            }
    }

    private fun fieldToCoach(coach: Coach): HashMap<String, Any?> {
        return hashMapOf(
            "mail" to coach.mail,
            "firstName" to coach.firstName,
            "lastName" to coach.lastName,
            "id" to coach.id,
            "createdDate" to coach.createdDate
        )
    }

}