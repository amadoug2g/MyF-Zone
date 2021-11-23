package com.myfzone_sport.myf_zone.data

import android.content.Context
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
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
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.coroutines.flow.Flow
import java.util.*

class RepositoryImpl(
//    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : Repository {

    override fun affiliateCoach(club: Club, sport: Sport, category: Category?, subCategory: SubCategory?) {
        remoteDataSource.affiliateCoach(
            club, sport, category, subCategory
        )
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

    override fun getClubFromId(clubId: String): Flow<State<Club>> {
        return remoteDataSource.getClubFromId(clubId)
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

    override fun acceptParticipant(eventId: String, participant: EventParticipant): Flow<State<EventParticipant>> {
        return remoteDataSource.acceptParticipant(eventId,participant)
    }

    override fun refuseParticipant(eventId: String, participant: EventParticipant): Flow<State<EventParticipant>> {
        return remoteDataSource.refuseParticipant(eventId, participant)
    }

    override fun joinEvent(eventId: String, participant: EventParticipant): Flow<State<EventParticipant>> {
        return remoteDataSource.joinEvent(eventId, participant)
    }

    override fun leaveEvent(eventId: String): Flow<State<Boolean>> {
        return remoteDataSource.leaveEvent(eventId)
    }

    override fun getEventFromId(eventId: String): Flow<State<Event>> {
        return remoteDataSource.getEventFromId(eventId)
    }

    override fun getOwnerFromEvent(eventId: String): Flow<State<EventOwner>> {
        return remoteDataSource.getOwnerFromEvent(eventId)
    }

    override fun getAllParticipantsList(eventId: String): Flow<State<MutableList<EventParticipant>>> {
        return remoteDataSource.getAllParticipantsList(eventId)
    }

    override fun getValidParticipantsList(): MutableList<EventParticipant> {
        return remoteDataSource.getValidParticipantsList()
    }

    override fun getValidParticipantsCount(): Int {
        return remoteDataSource.getValidParticipantsCount()
    }

    override fun addChatMessageListener(
        chatCoachId: String,
        context: Context,
        onListen: (List<Item>) -> Unit): ListenerRegistration? {
        return remoteDataSource.addChatMessageListener(chatCoachId, context, onListen)
    }

    override fun createChat(chatCoach: Coach, chatCoachClub: ClubAffiliation) {
        remoteDataSource.createChat(chatCoach, chatCoachClub)
    }

    override fun getDiscussionUser(chatCoachId: String): Flow<State<Coach>> {
        return remoteDataSource.getDiscussionUser(chatCoachId)
    }

    override fun getOrCreateChat(chatCoach: Coach, chatCoachClub: ClubAffiliation, message: String, photo: String) {
        remoteDataSource.getOrCreateChat(chatCoach, chatCoachClub, message, photo)
    }

    override fun getDiscussionUserClub(chatCoachId: String): Flow<State<ClubAffiliation>> {
        return remoteDataSource.getDiscussionUserClub(chatCoachId)
    }

    override fun sendChatMessage(chatCoach: Coach, message: String, photo: String) {
        remoteDataSource.sendChatMessage(chatCoach, message, photo)
    }

    override fun setDiscussionRead(chatCoach: Coach) {
        remoteDataSource.setDiscussionRead(chatCoach)
    }

    override fun setDiscussionUnread(chatCoach: Coach) {
        remoteDataSource.setDiscussionUnread(chatCoach)
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

    override fun createEvent(event: Event): Flow<State<Event>> {
        return remoteDataSource.createEvent(event)
    }

    override fun addNewEventToUser(event: Event, owner: EventOwner, club: ClubAffiliation): Flow<State<Event>> {
        return remoteDataSource.addNewEventToUser(event, owner, club)
    }

    override fun addOwnerToEvent(event: Event, owner: EventOwner): Flow<State<Boolean>> {
        return remoteDataSource.addOwnerToEvent(event, owner)
    }

    override fun getOwnerForNewEvent(): Flow<State<Pair<EventOwner, ClubAffiliation>>> {
        return remoteDataSource.getOwnerForNewEvent()
    }

    override fun getOwnerToken(ownerId: String): Flow<State<MutableList<String>>> {
        return remoteDataSource.getOwnerToken(ownerId)
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

    override fun getUserStatus(): Boolean {
        return remoteDataSource.getUserStatus()
    }

    override fun getUser(): Flow<State<Coach>> {
        return remoteDataSource.getUser()
    }

    override fun getUserClub(clubAffiliation: ClubAffiliation): Flow<State<Club?>> {
        return remoteDataSource.getUserClub(clubAffiliation)
    }

    override fun getUserAffiliation(coach: Coach): Flow<State<ClubAffiliation?>> {
        return remoteDataSource.getUserAffiliation(coach)
    }

    override fun getUserEventList(): Flow<State<MutableList<String>>> {
        return remoteDataSource.getUserEventList()
    }

    override fun getImageReference(path: String): StorageReference {
        return remoteDataSource.getImageReference(path)
    }

    override fun isUserOwner(eventId: String): Boolean {
        return remoteDataSource.isUserOwner(eventId)
    }

    override fun signOut() {
        remoteDataSource.signOut()
    }
}