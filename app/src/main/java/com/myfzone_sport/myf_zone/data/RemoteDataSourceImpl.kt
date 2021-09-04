package com.myfzone_sport.myf_zone.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
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
import com.myfzone_sport.myf_zone.fragments.calendar.CalendarService
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Constants.DB
import com.myfzone_sport.myf_zone.util.Constants.EVENT_PATH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.*

class RemoteDataSourceImpl : RemoteDataSource {
    override fun affiliateCoach() {
        TODO("Not yet implemented")
    }

    override fun getClubList(): MutableList<Club>? {
        TODO("Not yet implemented")
    }

    override fun getClubId(): String {
        TODO("Not yet implemented")
    }

    override fun getClubFromCode(): Club {
        TODO("Not yet implemented")
    }

    override fun getCategoryList(): MutableList<Category>? {
        TODO("Not yet implemented")
    }

    override fun getCategoryId(): String {
        TODO("Not yet implemented")
    }

    override fun getSubCategoryList(): MutableList<SubCategory>? {
        TODO("Not yet implemented")
    }

    override fun getSubCategoryId(): String {
        TODO("Not yet implemented")
    }

    override fun getSportList(): MutableList<Sport>? {
        TODO("Not yet implemented")
    }

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
            DB.collection(EVENT_PATH)
                .document(eventId)
                .collection("Owner")

        return try {
            docRef.get().await().documents[0].toObject<EventOwner>()!!
        } catch (e: Exception) {
            Log.e("getOwnerFromEvent", "Error in getOwnerFromEvent: $e")
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

    override fun getAllEvents() = flow {
        emit(State.loading())
        val now = Calendar.getInstance().time

        val mEventQuery = DB.collection(EVENT_PATH).orderBy("date").limit(15)

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

    override fun getFriendlyEvents(): MutableList<Event> {
        TODO("Not yet implemented")
    }

    override fun getTourneyEvents(): MutableList<Event> {
        TODO("Not yet implemented")
    }

    override fun getPlateauEvents(): MutableList<Event> {
        TODO("Not yet implemented")
    }

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

    override fun signInUser() {
        TODO("Not yet implemented")
    }

    override fun signUpUser() {
        TODO("Not yet implemented")
    }

    override fun addUserToDatabase() {
        TODO("Not yet implemented")
    }

    override fun assignDisplayName() {
        TODO("Not yet implemented")
    }

    override fun assignProfileImage() {
        TODO("Not yet implemented")
    }

    override fun getUser(): Coach {
        TODO("Not yet implemented")
    }

    override fun getUserClub(): Club {
        TODO("Not yet implemented")
    }

    override fun getUserAffiliation(): ClubAffiliation {
        TODO("Not yet implemented")
    }

    override fun getImageReference(): String {
        TODO("Not yet implemented")
    }

    override fun getUserEvents(): MutableList<Event> {
        TODO("Not yet implemented")
    }

    override fun isUserOwner(): Boolean {
        TODO("Not yet implemented")
    }

    override fun checkConnectedStatus(): Boolean {
        TODO("Not yet implemented")
    }

}