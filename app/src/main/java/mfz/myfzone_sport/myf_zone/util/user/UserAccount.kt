package mfz.myfzone_sport.myf_zone.util.user

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import mfz.myfzone_sport.myf_zone.model.club.Club
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import java.net.URL
import java.util.*


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

    private suspend fun getGlobalUser(): Coach? {
        val docRef = currentUserDocRef

        return try {
            docRef.get().await().toObject<Coach>()!!
        } catch (e: Exception) {
            Log.e(TAG, "e")
            null
        }
    }

    private suspend fun getGlobalUserClub(): Club? {
        val docRef =
            currentUserDocRef.collection("ClubAffiliation")

        return try {
            docRef.get().await().documents[0].toObject<Club>()!!
        } catch (e: Exception) {
            Log.e(TAG, "e")
            null
        }
    }

    suspend fun getClubUser(): ClubAffiliation? {
        val currentUser = auth.currentUser

        val docRef =
            DB.collection(COACH_PATH)
                .document(currentUser!!.uid)
                .collection("ClubAffiliation")

        return try {
            docRef.get().await().documents[0].toObject<ClubAffiliation>()!!
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
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

    suspend fun getCurrentClubId(): ClubAffiliation? {
        val docRef = currentUserDocRef.collection("ClubAffiliation")

        return try {
            val documents = docRef.get().await().documents
            val result: ClubAffiliation = documents[0].toObject()!!
            result
        } catch (e: Exception) {
            null
        }
    }

    fun pathToReference(path: String) =
        storageInstance.getReference(path.removePrefix("gs://myf-zone.appspot.com"))

    fun getImageClub(path: String): Bitmap? = runBlocking {
        withContext(IO) {
            Picasso.get().load(path).get()
        }
    }

    fun getImageClubURL(path: String): Bitmap? = runBlocking {
        withContext(IO) {
            val url = URL(path)
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
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