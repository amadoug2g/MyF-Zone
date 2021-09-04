package com.myfzone_sport.myf_zone.data

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
import kotlinx.coroutines.flow.FlowCollector

interface RemoteDataSource {

    //region Affiliation
    fun affiliateCoach()

    fun getClubList(): MutableList<Club>?

    fun getClubId(): String

    fun getClubFromCode(): Club

    fun getCategoryList(): MutableList<Category>?

    fun getCategoryId(): String

    fun getSubCategoryList(): MutableList<SubCategory>?

    fun getSubCategoryId(): String

    fun getSportList(): MutableList<Sport>?

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

    fun getFriendlyEvents(): MutableList<Event>

    fun getTourneyEvents(): MutableList<Event>

    fun getPlateauEvents(): MutableList<Event>

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
    fun signInUser()

    fun signUpUser()

    fun addUserToDatabase()

    fun assignDisplayName()

    fun assignProfileImage()
    //endregion

    //region User
    fun getUser(): Coach

    fun getUserClub(): Club

    fun getUserAffiliation(): ClubAffiliation

    fun getImageReference(): String

    fun getUserEvents(): MutableList<Event>

    fun isUserOwner(): Boolean

    fun checkConnectedStatus(): Boolean
    //endregion

    //region Map
    //endregion

    //region Settings
    //endregion
}