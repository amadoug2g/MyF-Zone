package com.myfzone_sport.myf_zone.data

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.app.framework.FirebaseService
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClubAffiliation
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

interface Repository {

    //region Affiliation
    fun affiliateCoach(club: Club, sport: Sport, category: Category?, subCategory: SubCategory?)

    fun getClubList(): Flow<State<MutableList<Club>>>

    fun getClubId(): String

    fun getClubFromCode(): Club

    fun getClubFromId(clubId: String): Flow<State<Club>>

    fun getCategoryList(sportId: String): Flow<State<MutableList<Category>>>

    fun getCategoryId(): String

    fun getSubCategoryList(sportId: String, categoryId: String): Flow<State<MutableList<SubCategory>>>

    fun getSubCategoryId(): String

    fun getSportList(): Flow<State<MutableList<Sport>>>

    fun getSportId(): String

    fun sendClubSuggestion()
    //endregion

    //region Event Detail
    fun acceptParticipant(eventId: String, participant: EventParticipant): Flow<State<EventParticipant>>

    fun refuseParticipant(eventId: String, participant: EventParticipant): Flow<State<EventParticipant>>

    fun joinEvent(eventId: String, participant: EventParticipant): Flow<State<EventParticipant>>

    fun leaveEvent(eventId: String): Flow<State<Boolean>>

    fun getEventFromId(eventId: String): Flow<State<Event>>

    fun getOwnerFromEvent(eventId: String): Flow<State<EventOwner>>

    fun getAllParticipantsList(eventId: String): Flow<State<MutableList<EventParticipant>>>

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
    fun createEvent(event: Event): Flow<State<Event>>

    fun addNewEventToUser(event: Event, owner: EventOwner, club: ClubAffiliation): Flow<State<Event>>

    fun addOwnerToEvent(event: Event, owner: EventOwner): Flow<State<Boolean>>

    fun getOwnerForNewEvent(): Flow<State<Pair<EventOwner, ClubAffiliation>>>
    //endregion

    //region Notification
    fun getOwnerToken(ownerId: String): Flow<State<MutableList<String>>>

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
    fun getUserStatus(): Boolean

    fun getUser(): Flow<State<Coach>>

    fun getUserClub(clubAffiliation: ClubAffiliation): Flow<State<Club?>>

    fun getUserAffiliation(coach: Coach): Flow<State<ClubAffiliation?>>

    fun getUserEventList(): Flow<State<MutableList<String>>>

    fun getImageReference(path: String = activeCoachClubAffiliation!!.clubLogo): StorageReference

    fun isUserOwner(eventId: String): Boolean

    fun signOut()
    //endregion

    //region Map
    //endregion

    //region Settings
    //endregion
}