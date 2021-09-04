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

class RepositoryImpl(
//    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : Repository {
    override fun affiliateCoach() {
        remoteDataSource.affiliateCoach()
    }

    override fun getClubList(): MutableList<Club>? {
        return remoteDataSource.getClubList()
    }

    override fun getClubId(): String {
        return remoteDataSource.getClubId()
    }

    override fun getClubFromCode(): Club {
        return remoteDataSource.getClubFromCode()
    }

    override fun getCategoryList(): MutableList<Category>? {
        return remoteDataSource.getCategoryList()
    }

    override fun getCategoryId(): String {
        return remoteDataSource.getCategoryId()
    }

    override fun getSubCategoryList(): MutableList<SubCategory>? {
        return remoteDataSource.getSubCategoryList()
    }

    override fun getSubCategoryId(): String {
        return remoteDataSource.getSubCategoryId()
    }

    override fun getSportList(): MutableList<Sport>? {
        return remoteDataSource.getSportList()
    }

    override fun getSportId(): String {
        return remoteDataSource.getSportId()
    }

    override fun sendClubSuggestion() {
        remoteDataSource.sendClubSuggestion()
    }

    override fun acceptParticipant() {
        remoteDataSource.acceptParticipant()
    }

    override fun refuseParticipant() {
        remoteDataSource.refuseParticipant()
    }

    override fun joinEvent() {
        remoteDataSource.joinEvent()
    }

    override fun leaveEvent() {
        remoteDataSource.leaveEvent()
    }

    override fun getEventFromId(): Club {
        return remoteDataSource.getEventFromId()
    }

    override suspend fun getOwnerFromEvent(eventId: String): EventOwner? {
        return remoteDataSource.getOwnerFromEvent(eventId)
    }

    override fun getAllParticipantsList(): MutableList<EventParticipant> {
        return remoteDataSource.getAllParticipantsList()
    }

    override fun getValidParticipantsList(): MutableList<EventParticipant> {
        return remoteDataSource.getValidParticipantsList()
    }

    override fun getValidParticipantsCount(): Int {
        return remoteDataSource.getValidParticipantsCount()
    }

    override fun createChat() {
        remoteDataSource.createChat()
    }

    override fun getDiscussionUser() {
        remoteDataSource.getDiscussionUser()
    }

    override fun getOrCreateChat() {
        remoteDataSource.getOrCreateChat()
    }

    override fun getDiscussionUserClub() {
        remoteDataSource.getDiscussionUserClub()
    }

    override fun sendChatMessage() {
        remoteDataSource.sendChatMessage()
    }

    override fun sendDiscussionRead() {
        remoteDataSource.sendDiscussionRead()
    }

    override fun sendDiscussionUnread() {
        remoteDataSource.sendDiscussionUnread()
    }

    override fun updateEvent() {
        remoteDataSource.updateEvent()
    }

    override fun updateEventForOwner() {
        remoteDataSource.updateEventForOwner()
    }

    override fun deleteEvent() {
        remoteDataSource.deleteEvent()
    }

    override fun getAllEvents(): Flow<State<MutableList<Event>>> {
        return remoteDataSource.getAllEvents()
    }

    override fun getFriendlyEvents(): MutableList<Event> {
        return remoteDataSource.getFriendlyEvents()
    }

    override fun getTourneyEvents(): MutableList<Event> {
        return remoteDataSource.getTourneyEvents()
    }

    override fun getPlateauEvents(): MutableList<Event> {
        return remoteDataSource.getPlateauEvents()
    }

    override fun createEvent() {
        remoteDataSource.createEvent()
    }

    override fun addNewEventToUser() {
        remoteDataSource.addNewEventToUser()
    }

    override fun addOwnerToEvent() {
        remoteDataSource.addOwnerToEvent()
    }

    override fun getOwnerForNewEvent() {
        remoteDataSource.getOwnerForNewEvent()
    }

    override fun getOwnerToken() {
        remoteDataSource.getOwnerToken()
    }

    override fun getParticipantsToken() {
        remoteDataSource.getParticipantsToken()
    }

    override fun notifyOwner() {
        remoteDataSource.notifyOwner()
    }

    override fun notifyParticipants() {
        remoteDataSource.notifyParticipants()
    }

    override fun signInUser() {
        remoteDataSource.signInUser()
    }

    override fun signUpUser() {
        remoteDataSource.signUpUser()
    }

    override fun addUserToDatabase() {
        remoteDataSource.addUserToDatabase()
    }

    override fun assignDisplayName() {
        remoteDataSource.assignDisplayName()
    }

    override fun assignProfileImage() {
        remoteDataSource.assignProfileImage()
    }

    override fun getUser(): Coach {
        return remoteDataSource.getUser()
    }

    override fun getUserClub(): Club {
        return remoteDataSource.getUserClub()
    }

    override fun getUserAffiliation(): ClubAffiliation {
        return remoteDataSource.getUserAffiliation()
    }

    override fun getImageReference(): String {
        return remoteDataSource.getImageReference()
    }

    override fun getUserEvents(): MutableList<Event> {
        return remoteDataSource.getUserEvents()
    }

    override fun isUserOwner(): Boolean {
        return remoteDataSource.isUserOwner()
    }

    override fun checkConnectedStatus(): Boolean {
        return remoteDataSource.checkConnectedStatus()
    }
}