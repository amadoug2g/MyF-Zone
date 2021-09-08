package com.myfzone_sport.myf_zone.data

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.domain.sport.Category
import com.myfzone_sport.myf_zone.domain.sport.Sport
import com.myfzone_sport.myf_zone.domain.sport.SubCategory
import kotlinx.coroutines.flow.Flow
import java.util.HashMap

interface RemoteDataSource {
    val firebaseFirestore: FirebaseFirestore
        get() = Firebase.firestore

//    val firebaseFirestore: FirebaseFirestore
//        get() = Firebase.firestore

    //region Affiliation
    fun affiliateCoach(
        code: String,
        affiliationSport: String,
        affiliationCategory: String?,
        affiliationSubCategory: String?
    )

    fun sendRequestToClub(
        clubId: String,
        affiliationRequest: HashMap<String, Any?>,
        removeListener: Boolean = false
    )

    fun getClubList(): Flow<State<MutableList<Club>>>

    fun getClubId(): String

    fun getClubFromCode(): Club

    fun getCategoryList(sportId: String): Flow<State<MutableList<Category>>>

    fun getCategoryId(): String

    fun getSubCategoryList(sportId: String, categoryId: String): Flow<State<MutableList<SubCategory>>>

    fun getSubCategoryId(): String

    fun getSportList(): Flow<State<MutableList<Sport>>>

    fun getSportId(): String

    fun sendClubSuggestion()
    //endregion

    //region Event Detail
    fun acceptParticipant()

    fun refuseParticipant()

    fun joinEvent()

    fun leaveEvent()

    fun getEventFromId(): Club

    suspend fun getOwnerFromEvent(eventId: String): EventOwner?

    fun getAllParticipantsList(): MutableList<EventParticipant>

    fun getValidParticipantsList(): MutableList<EventParticipant>

    fun getValidParticipantsCount(): Int


    //endregion

    //region Discussion
    fun createChat()

    fun getDiscussionUser()

    fun getOrCreateChat()

    fun getDiscussionUserClub()

    fun sendChatMessage()

    fun sendDiscussionRead()

    fun sendDiscussionUnread()
    //endregion

    //region Event Edit
    fun updateEvent()

    fun updateEventForOwner()

    fun deleteEvent()
    //endregion

    //region Event
    fun getAllEvents(): Flow<State<MutableList<Event>>>

    fun getCloseEvents(): Flow<State<MutableList<Event>>>

    fun getUserEvents(): Flow<State<MutableList<Event>>>

    fun getFriendlyEvents(): Flow<State<MutableList<Event>>>

    fun getTourneyEvents(): Flow<State<MutableList<Event>>>

    fun getPlateauEvents(): Flow<State<MutableList<Event>>>

    //endregion

    //region New Event
    fun createEvent()

    fun addNewEventToUser()

    fun addOwnerToEvent()

    fun getOwnerForNewEvent()
    //endregion

    //region Notification
    fun getOwnerToken()

    fun getParticipantsToken()

    fun notifyOwner()

    fun notifyParticipants()
    //endregion

    //region Registration
    fun signInUser(email: String, password: String): Flow<State<AuthResult>>

    fun signUpUser(email: String, password: String): Flow<State<FirebaseUser>>

    fun addUserToDatabase(coach: Coach): Flow<State<Coach>>

    fun assignDisplayName(coach: Coach): Flow<State<Boolean>>

    fun assignProfileImage()
    //endregion

    //region User
    fun getUser(user: FirebaseUser?)

    fun getUserAffiliation(user: FirebaseUser?)

    fun getImageReference(): String

    fun isUserOwner(eventId: String): Boolean

    fun getUserInfo(): Boolean

    fun signOut()
    //endregion

    //region Map
    //endregion

    //region Settings
    //endregion
}