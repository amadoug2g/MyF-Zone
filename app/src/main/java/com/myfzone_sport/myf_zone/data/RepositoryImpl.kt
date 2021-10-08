package com.myfzone_sport.myf_zone.data

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.StorageReference
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
import java.util.*

class RepositoryImpl(
//    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : Repository {
    override fun affiliateCoach(
        code: String,
        affiliationSport: String,
        affiliationCategory: String?,
        affiliationSubCategory: String?
    ) {
        remoteDataSource.affiliateCoach(
            code,
            affiliationSport,
            affiliationCategory,
            affiliationSubCategory
        )
    }

    override fun sendRequestToClub(
        clubId: String,
        affiliationRequest: HashMap<String, Any?>,
        removeListener: Boolean
    ) {
        remoteDataSource.sendRequestToClub(clubId, affiliationRequest, removeListener)
    }

    override fun getClubList(): Flow<State<MutableList<Club>>> {
        return remoteDataSource.getClubList()
    }

    override fun getClubId(): String {
        return remoteDataSource.getClubId()
    }

    override fun getClubFromCode(): Club {
        return remoteDataSource.getClubFromCode()
    }

    override fun getCategoryList(sportId: String): Flow<State<MutableList<Category>>> {
        return remoteDataSource.getCategoryList(sportId)
    }

    override fun getCategoryId(): String {
        return remoteDataSource.getCategoryId()
    }

    override fun getSubCategoryList(sportId: String, categoryId: String): Flow<State<MutableList<SubCategory>>> {
        return remoteDataSource.getSubCategoryList(sportId, categoryId)
    }

    override fun getSubCategoryId(): String {
        return remoteDataSource.getSubCategoryId()
    }

    override fun getSportList(): Flow<State<MutableList<Sport>>> {
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

    override fun getEventFromId(eventId: String): Flow<State<Event>> {
        return remoteDataSource.getEventFromId(eventId)
    }

    override fun getOwnerFromEvent(eventId: String): Flow<State<EventOwner>> {
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

    override fun getCloseEvents(): Flow<State<MutableList<Event>>> {
        return remoteDataSource.getCloseEvents()
    }

    override fun getUserEvents(): Flow<State<MutableList<Event>>> {
        return remoteDataSource.getUserEvents()
    }

    override fun getFriendlyEvents(): Flow<State<MutableList<Event>>> {
        return remoteDataSource.getFriendlyEvents()
    }

    override fun getTourneyEvents(): Flow<State<MutableList<Event>>> {
        return remoteDataSource.getTourneyEvents()
    }

    override fun getPlateauEvents(): Flow<State<MutableList<Event>>> {
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

    override fun signInUser(email: String, password: String): Flow<State<AuthResult>> {
        return remoteDataSource.signInUser(email, password)
    }

    override fun signUpUser(email: String, password: String): Flow<State<FirebaseUser>> {
        return remoteDataSource.signUpUser(email, password)
    }

    override fun addUserToDatabase(coach: Coach): Flow<State<Coach>> {
        return remoteDataSource.addUserToDatabase(coach)
    }

    override fun assignDisplayName(coach: Coach): Flow<State<Boolean>> {
        return remoteDataSource.assignDisplayName(coach)
    }

    override fun assignProfileImage() {
        remoteDataSource.assignProfileImage()
    }

    override fun getUser(user: FirebaseUser?) {
        remoteDataSource.getUser(user)
    }

    override fun getUserAffiliation(user: FirebaseUser?) {
        remoteDataSource.getUserAffiliation(user)
    }

    override fun getImageReference(): StorageReference {
        return remoteDataSource.getImageReference()
    }

    override fun isUserOwner(eventId: String): Boolean {
        return remoteDataSource.isUserOwner(eventId)
    }

    override fun getUserInfo(): Boolean  {
        return remoteDataSource.getUserInfo()
    }

    override fun signOut() {
        remoteDataSource.signOut()
    }
}