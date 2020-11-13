package com.example.myf_zone.util

import android.util.Log
import com.example.myf_zone.model.club.AffiliationRequest
import com.example.myf_zone.model.coach.ClubAffiliation
import com.example.myf_zone.model.coach.Coach
import com.example.myf_zone.util.StorageUtil.coachPath
import com.example.myf_zone.util.StorageUtil.db
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.util.*

object FirebaseUtil {
    private val TAG = FirebaseUtil::class.java.simpleName

    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    lateinit var auth: FirebaseAuth

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document(
            coachPath + "/${FirebaseAuth.getInstance().currentUser?.uid
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

        db.collection(coachPath)
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

    fun addAffiliationUser(affiliation: ClubAffiliation) {
        Log.d(TAG, "Adding ${affiliation.categoryName} as affiliation")
        val currentUser = auth.currentUser

        db.collection(coachPath)
            .document(currentUser!!.uid)
            .collection("ClubAffiliation")
            .add(affiliation)
            .addOnSuccessListener {
                Log.d(TAG, "Club affiliation added successfully")
            }
            .addOnFailureListener {
                Log.d(TAG, "Club affiliation added failed")
            }
            .addOnCompleteListener {
                Log.d(TAG, "Club affiliation added completed")
            }
    }

    fun userAffiliationStatus(myCallback: (Boolean) -> Unit) {
        val affiliationPath = currentUserDocRef.collection("ClubAffiliation")

        affiliationPath.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val documents = it.result.documents

                val result = documents.size > 0

                myCallback(result)
            }
        }
    }

    fun updateAffiliationStatus() {

    }

    fun checkRequestStatus(affiliationRequest: AffiliationRequest): Boolean {
        return (affiliationRequest.status == "validate")
    }
}