package com.myfzone_sport.myf_zone.app.framework

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClub
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliation
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachEvents
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.affiliationNbr
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.firebaseAuth
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.firebaseMsg
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isAffiliated
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isConnected
import com.myfzone_sport.myf_zone.data.RemoteDataSource
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.chat.MessagingService
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.domain.sport.Category
import com.myfzone_sport.myf_zone.domain.sport.Sport
import com.myfzone_sport.myf_zone.domain.sport.SubCategory
import com.myfzone_sport.myf_zone.util.Constants.CLUB_PATH
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB
import com.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import com.myfzone_sport.myf_zone.util.Constants.SPORT_PATH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.*

class RemoteDataSourceImpl : RemoteDataSource {
    private val TAG = "RemoteDataSource"
    override val firebaseFirestore = Firebase.firestore

    override fun affiliateCoach(
        code: String,
        affiliationSport: String,
        affiliationCategory: String?,
        affiliationSubCategory: String?
    ) {
        TODO("Not yet implemented")
    }

    override fun sendRequestToClub(
        clubId: String,
        affiliationRequest: HashMap<String, Any?>,
        removeListener: Boolean
    ) {
        val docRef = firebaseFirestore.collection(CLUB_PATH)
            .document(clubId)
            .collection("AffiliationRequest")
            .add(affiliationRequest)

        if (!removeListener) {
            docRef
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
    }

    override fun getClubList() = flow<State<MutableList<Club>>> {
        emit(State.loading())

        val mSportQuery = firebaseFirestore.collection(CLUB_PATH)

        val snapshot = mSportQuery.get().await()

        if (!snapshot.isEmpty) emit(State.success(snapshot.toObjects(Club::class.java)))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getClubId(): String {
        TODO("Not yet implemented")
    }

    override fun getClubFromCode(): Club {
        TODO("Not yet implemented")
    }

    override fun getCategoryList(sportId: String) = flow<State<MutableList<Category>>> {
        emit(State.loading())

        val mCategoryQuery =
            firebaseFirestore.collection(SPORT_PATH + "/${sportId}/Category").orderBy("rank")

        val snapshot = mCategoryQuery.get().await()

        if (!snapshot.isEmpty) emit(State.success(snapshot.toObjects(Category::class.java)))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getCategoryId(): String {
        TODO("Not yet implemented")
    }

    override fun getSubCategoryList(sportId: String, categoryId: String) =
        flow<State<MutableList<SubCategory>>> {
            emit(State.loading())

            val mCategoryQuery =
                firebaseFirestore.collection(SPORT_PATH + "/${sportId}/Category/${categoryId}/SubCategory")
                    .orderBy("rank")

            val snapshot = mCategoryQuery.get().await()

            if (!snapshot.isEmpty) emit(State.success(snapshot.toObjects(SubCategory::class.java)))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(Dispatchers.IO)

    override fun getSubCategoryId(): String {
        TODO("Not yet implemented")
    }

    override fun getSportList() = flow<State<MutableList<Sport>>> {
        emit(State.loading())

        val mSportQuery = firebaseFirestore.collection(SPORT_PATH)

        val snapshot = mSportQuery.get().await()

        if (!snapshot.isEmpty) emit(State.success(snapshot.toObjects(Sport::class.java)))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getSportId(): String {
        TODO("Not yet implemented")
    }

    override fun sendClubSuggestion() {
        TODO("Not yet implemented")
    }

    override fun acceptParticipant() {
        TODO("Not yet implemented")
    }

    override fun refuseParticipant() {
        TODO("Not yet implemented")
    }

    override fun joinEvent() {
        TODO("Not yet implemented")
    }

    override fun leaveEvent() {
        TODO("Not yet implemented")
    }

    override fun getEventFromId(): Club {
        TODO("Not yet implemented")
    }

    override suspend fun getOwnerFromEvent(eventId: String): EventOwner? {
        val docRef =
            firebaseFirestore.collection(EVENT_PATH)
                .document(eventId)
                .collection("Owner")

        return try {
            docRef.get().await().documents[0].toObject<EventOwner>()!!
        } catch (e: Exception) {
            Log.e("getOwnerFromEvent", "Error: $e")
            null
        }
    }

    override fun getAllParticipantsList(): MutableList<EventParticipant> {
        TODO("Not yet implemented")
    }

    override fun getValidParticipantsList(): MutableList<EventParticipant> {
        TODO("Not yet implemented")
    }

    override fun getValidParticipantsCount(): Int {
        TODO("Not yet implemented")
    }

    override fun createChat() {
        TODO("Not yet implemented")
    }

    override fun getDiscussionUser() {
        TODO("Not yet implemented")
    }

    override fun getOrCreateChat() {
        TODO("Not yet implemented")
    }

    override fun getDiscussionUserClub() {
        TODO("Not yet implemented")
    }

    override fun sendChatMessage() {
        TODO("Not yet implemented")
    }

    override fun sendDiscussionRead() {
        TODO("Not yet implemented")
    }

    override fun sendDiscussionUnread() {
        TODO("Not yet implemented")
    }

    override fun updateEvent() {
        TODO("Not yet implemented")
    }

    override fun updateEventForOwner() {
        TODO("Not yet implemented")
    }

    override fun deleteEvent() {
        TODO("Not yet implemented")
    }

    override fun getAllEvents(): Flow<State<MutableList<Event>>> = flow {
        emit(State.loading())
//        val now = Calendar.getInstance().time

        val mEventQuery = firebaseFirestore.collection(EVENT_PATH).orderBy("date").limit(15)

        val snapshot = mEventQuery.get().await()
        val eventList: MutableList<Event> = mutableListOf()

        snapshot.forEach {
            val event = it.toObject<Event>()
//            if (event.date.time > now.time)
            eventList.add(event)
        }

//        eventList.forEach { event ->
//            val owner = getOwnerFromEvent(event.id)
//            val participantList = getParticipantsFromEvent(event.id)
//
//            event.owner = owner!!
//            event.participants = participantList!!
//        }

        emit(State.success(eventList))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getFriendlyEvents(): Flow<State<MutableList<Event>>> = flow {
        emit(State.loading())
//        val now = Calendar.getInstance().time

        val mEventQuery = firebaseFirestore.collection(EVENT_PATH).orderBy("date").limit(15)
            .whereEqualTo("type", "friendly")

        val snapshot = mEventQuery.get().await()
        val eventList: MutableList<Event> = mutableListOf()

        snapshot.forEach {
            val event = it.toObject<Event>()
//            if (event.date.time > now.time)
            eventList.add(event)
        }

//        eventList.forEach { event ->
//            val owner = getOwnerFromEvent(event.id)
//            val participantList = getParticipantsFromEvent(event.id)
//
//            event.owner = owner!!
//            event.participants = participantList!!
//        }

        emit(State.success(eventList))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getTourneyEvents(): Flow<State<MutableList<Event>>> = flow {
        emit(State.loading())
//        val now = Calendar.getInstance().time

        val mEventQuery = firebaseFirestore.collection(EVENT_PATH).orderBy("date").limit(15)
            .whereEqualTo("type", "tournament")

        val snapshot = mEventQuery.get().await()
        val eventList: MutableList<Event> = mutableListOf()

        snapshot.forEach {
            val event = it.toObject<Event>()
//            if (event.date.time > now.time)
            eventList.add(event)
        }

//        eventList.forEach { event ->
//            val owner = getOwnerFromEvent(event.id)
//            val participantList = getParticipantsFromEvent(event.id)
//
//            event.owner = owner!!
//            event.participants = participantList!!
//        }

        emit(State.success(eventList))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getPlateauEvents(): Flow<State<MutableList<Event>>> = flow {
        emit(State.loading())
//        val now = Calendar.getInstance().time

        val mEventQuery = firebaseFirestore.collection(EVENT_PATH).orderBy("date").limit(15)
            .whereEqualTo("type", "plateau")

        val snapshot = mEventQuery.get().await()
        val eventList: MutableList<Event> = mutableListOf()

        snapshot.forEach {
            val event = it.toObject<Event>()
//            if (event.date.time > now.time)
            eventList.add(event)
        }

//        eventList.forEach { event ->
//            val owner = getOwnerFromEvent(event.id)
//            val participantList = getParticipantsFromEvent(event.id)
//
//            event.owner = owner!!
//            event.participants = participantList!!
//        }

        emit(State.success(eventList))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun createEvent() {
        TODO("Not yet implemented")
    }

    override fun addNewEventToUser() {
        TODO("Not yet implemented")
    }

    override fun addOwnerToEvent() {
        TODO("Not yet implemented")
    }

    override fun getOwnerForNewEvent() {
        TODO("Not yet implemented")
    }

    override fun getOwnerToken() {
        TODO("Not yet implemented")
    }

    override fun getParticipantsToken() {
        TODO("Not yet implemented")
    }

    override fun notifyOwner() {
        TODO("Not yet implemented")
    }

    override fun notifyParticipants() {
        TODO("Not yet implemented")
    }

    override fun signInUser(email: String, password: String) = flow<State<AuthResult>> {
        emit(State.loading())

        val auth = firebaseAuth.signInWithEmailAndPassword(email, password).await()

        firebaseMsg.token.addOnCompleteListener(OnCompleteListener { task ->
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
    }.flowOn(Dispatchers.IO)

    override fun signUpUser(email: String, password: String) = flow {
        emit(State.loading())

        val auth = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

        emit(State.success(auth.user!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun addUserToDatabase(coach: Coach) = flow {
        val user = firebaseAuth.currentUser
        val mDataBaseQuery = DB.document(COACH_PATH + "/${user?.uid}")

        emit(State.loading())

        mDataBaseQuery.set(coach.toMap()).await()

        emit(State.success(coach))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun assignDisplayName(coach: Coach) = flow {
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
    }.flowOn(Dispatchers.IO)

    override fun assignProfileImage() {
        TODO("Not yet implemented")
    }

    override fun getImageReference(): String {
        TODO("Not yet implemented")
    }

    override fun isUserOwner(eventId: String): Boolean {
        return (activeCoachEvents.contains(eventId))
    }

    override fun getUserInfo() {
        try {
            val user: FirebaseUser? = firebaseAuth.currentUser
            if (user != null) {
                isConnected = true

                val mAffiliationPath = firebaseFirestore
                    .collection(COACH_PATH + "/${user.uid}/ClubAffiliation")

                mAffiliationPath.get().addOnSuccessListener {

                    if (it.documents.size > 0) {
                        isAffiliated = true
                        getUser(user)
                        getUserAffiliation(user)
                    } else {
                        activeCoachEvents = mutableListOf()
                        isAffiliated = false
                        activeCoachClubAffiliation = null
                    }
                }
            } else {
                activeCoachEvents = mutableListOf()
                isConnected = false
                isAffiliated = false
                activeCoach = null
                activeCoachClubAffiliation = null
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error: ${e.localizedMessage}")
        }
    }

    override fun getUser(user: FirebaseUser?) {
        val mUserQuery = firebaseFirestore.document(COACH_PATH + "/${user?.uid}")

        mUserQuery.get().addOnSuccessListener {
            activeCoach = it.toObject(Coach::class.java)
        }
    }

    override fun getUserClub(affiliation: ClubAffiliation?) {
        val mClubQuery = firebaseFirestore.document(CLUB_PATH + "/${affiliation?.clubId}")

        mClubQuery.get().addOnSuccessListener {
            activeCoachClub = it.toObject(Club::class.java)
        }
    }

    override fun getUserAffiliation(user: FirebaseUser?) {
        val mClubQuery = firebaseFirestore.collection(COACH_PATH + "/${user?.uid}/ClubAffiliation")

        mClubQuery.get().addOnSuccessListener {
            val snapshot = it.documents[affiliationNbr]
            activeCoachClubAffiliation = snapshot.toObject(ClubAffiliation::class.java)

            getUserClub(activeCoachClubAffiliation)
            getEventsList(user, activeCoachClubAffiliation)
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    private fun getEventsList(user: FirebaseUser?, affiliation: ClubAffiliation?) {
        activeCoachEvents = mutableListOf()
        val mEventsQuery =
            firebaseFirestore.collection(COACH_PATH + "/${user?.uid}/ClubAffiliation/${affiliation?.clubId}/CoachEvent")

        mEventsQuery.get().addOnSuccessListener {
            for (doc in it) {
                activeCoachEvents.add(doc.id)
            }
        }
    }
}