package com.myfzone_sport.myf_zone.app.framework

import android.location.Location
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClub
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliation
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliationLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachEvents
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachEventsLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.affiliationNbr
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.firebaseAuth
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.firebaseMsg
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isAffiliated
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isAffiliatedLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isConnected
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isConnectedLive
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.storageInstance
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
import com.myfzone_sport.myf_zone.util.Constants
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

    override fun joinEvent(eventId: String, participant: EventParticipant) = flow {
        val mParticipantQuery = Constants.DB
            .document(EVENT_PATH + "/${eventId}/Participant/${participant.coachId}")

        emit(State.loading())

        mParticipantQuery.set(participant.toMap()).await()

        emit(State.success(participant))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun leaveEvent(eventId: String) = flow {
        val userId = firebaseAuth.currentUser?.uid
        val mParticipantQuery = DB
            .document(EVENT_PATH + "/${eventId}/Participant/${userId}")

        emit(State.loading())

        mParticipantQuery.delete().await()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getEventFromId(eventId: String) = flow {
        val mEventQuery = DB.document(EVENT_PATH + "/${eventId}")

        emit(State.loading())

        val snapshot = mEventQuery.get().await()
        val event = snapshot.toObject(Event::class.java)

        emit(State.success(event!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getOwnerFromEvent(eventId: String) = flow {
        val mEventOwnerQuery = DB
            .collection(EVENT_PATH + "/${eventId}/Owner")

        emit(State.loading())

        val snapshot = mEventOwnerQuery.get().await().documents[0]
        val eventOwner = snapshot.toObject(EventOwner::class.java)

        emit(State.success(eventOwner!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getAllParticipantsList(eventId: String) =
        flow<State<MutableList<EventParticipant>>> {
            emit(State.loading())

            val mParticipantList = DB.collection(EVENT_PATH + "/${eventId}/Participant")

            val snapshot = mParticipantList.get().await()

            val resultState =
                if (!snapshot.isEmpty) (State.success(snapshot.toObjects(EventParticipant::class.java))) else (State.success(
                    mutableListOf()
                ))

            emit(resultState)
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(Dispatchers.IO)

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

        val mEventQuery = firebaseFirestore.collection(EVENT_PATH).orderBy("date")//.limit(15)

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

    override fun getCloseEvents(): Flow<State<MutableList<Event>>> = flow {
        emit(State.loading())
//        val now = Calendar.getInstance().time

        val mEventQuery = firebaseFirestore.collection(EVENT_PATH).orderBy("date")

        val snapshot = mEventQuery.get().await()
        val eventList: MutableList<Event> = mutableListOf()

        snapshot.forEach {
            val event = it.toObject<Event>()
//            if (event.date.time > now.time)

            if (!isUserOwner(event.id)) eventList.add(event)
        }

        val results = floatArrayOf(12F)

        val tree = TreeMap<Float, Event>()

        for (i in eventList) {
            Location.distanceBetween(
                activeCoachClub!!.lat,
                activeCoachClub!!.lng,
                i.lat,
                i.lng, results
            )

            tree[results[0]] = i
        }

        eventList.clear()

        for (j in tree) {
            eventList.add(j.value)
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

    override fun getUserEvents(): Flow<State<MutableList<Event>>> = flow {
        emit(State.loading())
//        val now = Calendar.getInstance().time

        val mCoachEventQuery = firebaseFirestore
            .collection(COACH_PATH)
            .document(activeCoach!!.id)
            .collection("ClubAffiliation")
            .document(activeCoachClubAffiliation!!.clubId)
            .collection("CoachEvent").orderBy("date")

        val snapshot = mCoachEventQuery.get().await()
        val eventList: MutableList<Event> = mutableListOf()

        snapshot.forEach {
            val event = it.toObject<Event>()
//            if (event.date.time > now.time)
            eventList.add(event)
        }

        emit(State.success(eventList))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun addEventListener(
        onListen: (MutableList<Event>) -> Unit
    ): ListenerRegistration? {
        val now = Calendar.getInstance().time

        val mUserChatQuery = DB
            .collection(EVENT_PATH)
        return try {
            mUserChatQuery
//                .orderBy("createdDate")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "Error in addEventListener", error)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Event>()
                    value?.documents?.forEach {

                        try {
                            val tempEvent = it.toObject(Event::class.java)!!
                            if (tempEvent.date.time > now.time)
                                items.add(it.toObject(Event::class.java)!!)
                        } catch (e: Exception) {
                            Log.i(TAG, "Error when fetching events: $e")
                        }
                    }

                    onListen(items)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in addEventListener: ${e.localizedMessage}")
            null
        }
    }

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

        Log.i("TAG getFriendlyEvents", "eventlist count: ${eventList.size}")

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

    override fun createEvent(event: Event) = flow {
        val mEventQuery = DB.collection(EVENT_PATH)
        event.id = mEventQuery.document().id

        emit(State.loading())

        mEventQuery.document(event.id).set(event.toMap()).await()

        emit(State.success(event))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun addNewEventToUser(event: Event, owner: EventOwner, club: ClubAffiliation) =
        flow {
            val mCoachEventQuery = DB
                .document(COACH_PATH + "/${owner.coachId}/ClubAffiliation/${club.clubId}/CoachEvent/${event.id}")

            emit(State.loading())

            mCoachEventQuery.set(event.toMap()).await()

            emit(State.success(event))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(Dispatchers.IO)

    override fun addOwnerToEvent(event: Event, owner: EventOwner) = flow<State<Boolean>> {
        val mEventQuery = DB
            .document(EVENT_PATH + "/${event.id}/Owner/${owner.coachId}")

        mEventQuery.set(owner.toMap()).await()

        emit(State.success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getOwnerForNewEvent() = flow {
        val userId = activeCoach?.id

        val mClubQuery = DB
            .collection(COACH_PATH + "/${userId}/ClubAffiliation")

        emit(State.loading())

        val snapshot = mClubQuery.get().await().documents[0]
        val currentUserClub = snapshot.toObject(ClubAffiliation::class.java)!!

        val eventOwner = EventOwner().clubToOwner()

        val pair: Pair<EventOwner, ClubAffiliation> = Pair(eventOwner, currentUserClub)

        emit(State.success(pair))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getOwnerToken(ownerId: String) = flow<State<MutableList<String>>> {
        val mOwnerTokenQuery = Constants.DB.document(Constants.COACH_PATH + "/${ownerId}")

        val snapshot = mOwnerTokenQuery.get().await()
        val user: Coach = snapshot.toObject()!!

        val tokenList = mutableListOf<String>()

        if (!user.devices.isNullOrEmpty()) {
            user.devices.forEach { tokenList.add(it) }
            emit(State.success(tokenList))
            Log.i(TAG, "Tokens: $tokenList")
        } else {
            emit(State.success(mutableListOf()))
            Log.i(TAG, "Tokens: list is empty")
        }
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

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

    override fun getImageReference(path: String): StorageReference {
        return storageInstance.getReference((path).removePrefix("gs://myf-zone.appspot.com"))
    }

    override fun isUserOwner(eventId: String): Boolean {
        return (activeCoachEvents.contains(eventId))
    }

    override fun getUser(): Flow<State<Coach>> = flow {
        val user: FirebaseUser? = firebaseAuth.currentUser
        val mUserQuery = firebaseFirestore.document(COACH_PATH + "/${user?.uid}")

        emit(State.loading())

        val snapshotUser = mUserQuery.get().await()

        val coach = snapshotUser.toObject(Coach::class.java)!!

        emit(State.success(coach))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getUserClub(): Flow<State<Club?>> = flow {
        val user: FirebaseUser? = firebaseAuth.currentUser
        val userId = activeCoach?.id
        val mClubQuery = firebaseFirestore.collection(COACH_PATH + "/${userId}/ClubAffiliation")

        emit(State.loading())

        val snapshotClub = mClubQuery.get().await()

        val club = snapshotClub.documents[0].toObject(Club::class.java)

        Log.i("TAG","state club! $club")

        emit(State.Success(club))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getUserAffiliation(): Flow<State<ClubAffiliation?>> = flow {
        val mAffiliationClubQuery =
            firebaseFirestore.document(CLUB_PATH + "/${activeCoachClubAffiliation?.clubId}")

        emit(State.loading())

        val snapshotAffiliation = mAffiliationClubQuery.get().await()

        val affiliation = snapshotAffiliation.toObject(ClubAffiliation::class.java)

        Log.i("TAG","state affiliation! $affiliation")

        emit(State.Success(affiliation))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getUserEventList() = flow {
        val user: FirebaseUser? = firebaseAuth.currentUser
//        activeCoachEvents = mutableListOf()

        val mEventListQuery =
            firebaseFirestore.collection(COACH_PATH + "/${user?.uid}/ClubAffiliation/${activeCoachClubAffiliation?.clubId}/CoachEvent")

        emit(State.loading())

        val snapshotEventList = mEventListQuery.get().await()

        val result = mutableListOf<String>()

        snapshotEventList.forEach {
//            if (!activeCoachEvents.contains(it.id)) activeCoachEvents.add(it.id)
            result.add(it.id)
        }

        activeCoachEvents = result

        emit(State.Success(result))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    fun getCurrentUserAll(): Flow<State<Boolean>> = flow {
        //region Path
        activeCoachEvents = mutableListOf()
        val user: FirebaseUser? = firebaseAuth.currentUser
        val mUserQuery = firebaseFirestore.document(COACH_PATH + "/${user?.uid}")
        val mClubQuery = firebaseFirestore.collection(COACH_PATH + "/${user?.uid}/ClubAffiliation")
        val mAffiliationClubQuery =
            firebaseFirestore.document(CLUB_PATH + "/${activeCoachClubAffiliation?.clubId}")
        val mEventListQuery =
            firebaseFirestore.collection(COACH_PATH + "/${user?.uid}/ClubAffiliation/${activeCoachClubAffiliation?.clubId}/CoachEvent")
        //endregion

        emit(State.loading())

        //region SnapShot
        val snapshotUser = mUserQuery.get().await()
        val snapshotClub = mClubQuery.get().await()
        val snapshotAffiliation = mAffiliationClubQuery.get().await()
        val snapshotEventList = mEventListQuery.get().await()
        //endregion

        //region Query
        val club = snapshotClub.documents[0].toObject(Club::class.java)
        val coach = snapshotUser.toObject(Coach::class.java)
        val affiliation = snapshotAffiliation.toObject(ClubAffiliation::class.java)
        //endregion

        //region Assign
        snapshotEventList.forEach {
            if (!activeCoachEvents.contains(it.id)) activeCoachEvents.add(it.id)
        }

        activeCoach = coach
        activeCoachClub = club
        activeCoachClubAffiliation = affiliation
        //endregion

//        emit(State.Success(club))
//        emit(State.success(coach))
//        emit(State.Success(affiliation))

        emit(State.Success(true))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(Dispatchers.IO)

    override fun getUserStatus(): Boolean {
        val user: FirebaseUser? = firebaseAuth.currentUser
        return if (user != null) {
            isConnected = true
            isConnectedLive.postValue(true)
            getUserInfo()

            true
        } else {
            activeCoachEvents = mutableListOf()
            isConnected = false
            isAffiliated = false
            activeCoach = null
            activeCoachClubAffiliation = null


            activeCoachEventsLive.postValue(mutableListOf())
            isAffiliatedLive.postValue(false)
            isConnectedLive.postValue(false)
            activeCoachLive.postValue(null)
            activeCoachClubAffiliationLive.postValue(null)

            false
        }
    }

    /*override*/ suspend fun getUserAffiliationStatus(): Boolean {
        val mAffiliationPath =
            firebaseFirestore.collection(COACH_PATH + "/${firebaseAuth.currentUser!!.uid}/ClubAffiliation")

        val snapshot = mAffiliationPath.get().await()

        return if (snapshot.isEmpty) {
            isAffiliated = true

            snapshot.isEmpty
        } else {
            activeCoachEvents = mutableListOf()
            isAffiliated = false
            activeCoachClubAffiliation = null

            snapshot.isEmpty
        }

//            return (snapshot.isEmpty)
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    private fun getUserInfo(): Boolean {
        val user: FirebaseUser? = firebaseAuth.currentUser
        return if (user != null) {
            val mAffiliationPath = firebaseFirestore
                .collection(COACH_PATH + "/${user.uid}/ClubAffiliation")

            mAffiliationPath.get().addOnSuccessListener {

                if (it.documents.size > 0) {

                    isAffiliatedLive.postValue(true)
                    isAffiliated = true
                    getUser(user)
                    getUserAffiliation(user)
                } else {
                    activeCoachEvents = mutableListOf()
                    isAffiliated = false
                    activeCoachClubAffiliation = null

                    activeCoachEventsLive.postValue(mutableListOf())
                    isAffiliatedLive.postValue(false)
                    activeCoachClubAffiliationLive.postValue(null)
                }
            }
            true
        } else {
            activeCoachEvents = mutableListOf()
            isConnected = false
            isAffiliated = false
            activeCoach = null
            activeCoachClubAffiliation = null

            activeCoachEventsLive.postValue(mutableListOf())
            isAffiliatedLive.postValue(false)
            isConnectedLive.postValue(false)
            activeCoachLive.postValue(null)
            activeCoachClubAffiliationLive.postValue(null)

            false
        }
    }

    private fun getUser(user: FirebaseUser?) {
        val mUserQuery = firebaseFirestore.document(COACH_PATH + "/${user?.uid}")

        mUserQuery.get().addOnSuccessListener {
            activeCoach = it.toObject(Coach::class.java)
            activeCoachLive.postValue(it.toObject(Coach::class.java))
        }
    }

    private fun getUserAffiliation(user: FirebaseUser?) {
        val mClubQuery = firebaseFirestore.collection(COACH_PATH + "/${user?.uid}/ClubAffiliation")

        mClubQuery.get().addOnSuccessListener {
            val snapshot = it.documents[affiliationNbr]
            activeCoachClubAffiliation = snapshot.toObject(ClubAffiliation::class.java)
            activeCoachClubAffiliationLive.postValue(snapshot.toObject(ClubAffiliation::class.java))

            getUserClub(activeCoachClubAffiliation)
            getEventsList(user, activeCoachClubAffiliation)
        }
    }

    private fun getUserClub(affiliation: ClubAffiliation?) {
        val mClubQuery = firebaseFirestore.document(CLUB_PATH + "/${affiliation?.clubId}")

        mClubQuery.get().addOnSuccessListener {
            activeCoachClub = it.toObject(Club::class.java)
            activeCoachClubLive.postValue(it.toObject(Club::class.java))
        }
    }

    private fun getEventsList(user: FirebaseUser?, affiliation: ClubAffiliation?) {
        activeCoachEvents = mutableListOf()
        activeCoachEventsLive.postValue(mutableListOf())

        val mEventsQuery =
            firebaseFirestore.collection(COACH_PATH + "/${user?.uid}/ClubAffiliation/${affiliation?.clubId}/CoachEvent")

        mEventsQuery.get().addOnSuccessListener {
            for (doc in it) {
                if (!activeCoachEvents.contains(doc.id)) activeCoachEvents.add(doc.id)
                if (activeCoachEventsLive.value?.contains(doc.id) == false) activeCoachEventsLive.value?.add(
                    doc.id
                )
            }
        }
    }
}