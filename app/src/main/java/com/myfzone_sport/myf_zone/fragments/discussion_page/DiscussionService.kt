package com.myfzone_sport.myf_zone.fragments.discussion_page

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.chat.Chat
import com.myfzone_sport.myf_zone.model.chat.Message
import com.myfzone_sport.myf_zone.model.chat.TextMessageItem
import com.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.model.coach.Coach
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Created by Amadou on 07/12/2020, 12:04
 *
 * Discussion Page Service
 *
 */

object DiscussionService {
    private val TAG = this::class.java.simpleName
    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun getUserClub(userId: String) = flow<State<ClubAffiliation>> {
        val mClubQuery = DB
            .collection(COACH_PATH + "/${userId}/ClubAffiliation")

        emit(State.loading())

        val snapshot = mClubQuery.get().await().documents[0]
        val currentUserClub = snapshot.toObject(ClubAffiliation::class.java)

        emit(State.success(currentUserClub!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun getDiscussionUser(coachId: String) = flow<State<Coach>> {
        val mUserQuery = DB.document(COACH_PATH + "/${coachId}")

        emit(State.loading())

        val snapshot = mUserQuery.get().await()
        val coach = snapshot.toObject(Coach::class.java)

        emit(State.success(coach!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

    fun getOrCreateChat(
        other: Coach,
        otherClub: ClubAffiliation,
        message: String,
        photo: String
    ) {
        val mUserChatQuery = DB
            .document(COACH_PATH + "/${ManagerAuth.activeCoach!!.id}/Chat/${other.id}")

        mUserChatQuery.get().addOnSuccessListener {
            if (!it.exists()) {
                createChat(other, otherClub)
            } else {
                Log.i("DiscussionService", "[getOrCreateChat] Document already exists")
            }
            sendChatMessage(other, message, photo)
        }
    }

    private fun createChat(
        other: Coach,
        otherClub: ClubAffiliation
    ) {
        val time = Calendar.getInstance().time
        val mUserChatQuery = DB
            .document(COACH_PATH + "/${ManagerAuth.activeCoach!!.id}/Chat/${other.id}")

        val mOtherUserChatQuery = DB
            .document(COACH_PATH + "/${other.id}/Chat/${ManagerAuth.activeCoach!!.id}")

        val newUserChat: Chat = Chat().apply {
            coachId = other.id
            fullname = other.getName()
            clubLogo = otherClub.clubLogo
            isTyping = false
            lastMessage = ""
            unread = false
            createdDate = time
            updatedDate = time
        }

        val newOtherChat: Chat = Chat().apply {
            coachId = ManagerAuth.activeCoach!!.id
            fullname = ManagerAuth.activeCoach!!.getName()
            clubLogo = ManagerAuth.activeCoachClubAffiliation!!.clubLogo
            isTyping = false
            lastMessage = ""
            unread = false
            createdDate = time
            updatedDate = time
        }

        mUserChatQuery
            .set(newUserChat.toMap())

        mOtherUserChatQuery
            .set(newOtherChat.toMap())
    }

    private fun sendChatMessage(
        other: Coach,
        message: String,
        photo: String
    ) {
        val time = Calendar.getInstance().time
        val mUserChatQuery = DB
            .document(COACH_PATH + "/${ManagerAuth.activeCoach!!.id}/Chat/${other.id}")

        val mOtherUserChatQuery = DB
            .document(COACH_PATH + "/${other.id}/Chat/${ManagerAuth.activeCoach!!.id}")

        val messageId = mUserChatQuery.collection("Message").document().id

        val updatedUserChat = hashMapOf(
            "isTyping" to false,
            "lastMessage" to message,
            "unread" to false,
            "updatedDate" to time
        )

        val updatedOtherChat = hashMapOf(
            "isTyping" to false,
            "lastMessage" to message,
            "unread" to true,
            "updatedDate" to time
        )

        mUserChatQuery
            .set(updatedUserChat, SetOptions.merge())

        mOtherUserChatQuery
            .set(updatedOtherChat, SetOptions.merge())

        val newMessage = Message().apply {
            id = messageId
            senderId = ManagerAuth.activeCoach!!.id
            senderName = ManagerAuth.activeCoach!!.getName()
            senderClubLogo = ManagerAuth.activeCoachClubAffiliation!!.clubLogo
            text = message
            image = photo
            createdDate = time
        }

        val messageUserPath =
            mUserChatQuery
                .collection("Message")
                .document(messageId)

        messageUserPath.set(newMessage.toMap())

        val messageOtherPath =
            mOtherUserChatQuery
                .collection("Message")
                .document(messageId)

        messageOtherPath.set(newMessage.toMap())
    }

    fun addChatMessageListener(
        otherId: String,
        context: Context,
        onListen: (List<Item>) -> Unit
    ): ListenerRegistration? {
        val userId = firebaseAuth.currentUser?.uid

        val mUserChatQuery = DB
            .document(COACH_PATH + "/${userId}/Chat/${otherId}")
        return try {
            mUserChatQuery
                .collection("/Message").orderBy("createdDate")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e(TAG, "Error in addChatMessageListener", error)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    value?.documents?.forEach {
                        try {
                            items.add(TextMessageItem(it.toObject(Message::class.java)!!, context))
                        } catch (e: Exception) {
                            Log.i(TAG, "Error when fetching messages: $e")
                        }
                    }

                    onListen(items)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in addChatMessageListener: ${e.localizedMessage}")
            null
        }
    }

    fun setDiscussionRead(
        other: Coach
    ) {
        val mUserChatQuery = DB
            .document(COACH_PATH + "/${ManagerAuth.activeCoach!!.id}/Chat/${other.id}")

        mUserChatQuery.get().addOnSuccessListener {
            if (it.exists()) {
                val updatedUserChat = hashMapOf(
                    "unread" to false
                )

                mUserChatQuery
                    .set(updatedUserChat, SetOptions.merge())

            } else {
                Log.i(TAG, "[setDiscussionRead] Document does not exist")
            }
        }
    }

    fun setDiscussionUnread(
        other: Coach
    ) {
        val mUserChatQuery = DB
            .document(COACH_PATH + "/${ManagerAuth.activeCoach!!.id}/Chat/${other.id}")

        val updatedUserChat = hashMapOf(
            "unread" to true
        )

        mUserChatQuery
            .set(updatedUserChat, SetOptions.merge())
    }

    fun discussionHasMessages(
        other: Coach
    ) = flow<State<Boolean>> {
        val mChatMessagesCollection = DB
            .collection(COACH_PATH + "/${ManagerAuth.activeCoach!!.id}/Chat/${other.id}/Message")

        val mUserChatQuery = DB
            .document(COACH_PATH + "/${ManagerAuth.activeCoach!!.id}/Chat/${other.id}")

        val mOtherUserChatQuery = DB
            .document(COACH_PATH + "/${other.id}/Chat/${ManagerAuth.activeCoach!!.id}")

        mChatMessagesCollection.get().addOnSuccessListener { snapshot ->
            if (snapshot.documents.size > 0) {
                return@addOnSuccessListener
            } else {
                mUserChatQuery.delete()
                mOtherUserChatQuery.delete()
            }
        }

        emit(State.success(true))
    }

    fun setUserTyping(
        other: Coach, bool: Boolean
    ) {
        val mUserChatQuery = DB
            .document(COACH_PATH + "/${ManagerAuth.activeCoach!!.id}/Chat/${other.id}")

        mUserChatQuery.get().addOnSuccessListener {
            if (it.exists()) {
                val updatedUserChat = hashMapOf(
                    "isTyping" to bool
                )

                mUserChatQuery
                    .set(updatedUserChat, SetOptions.merge())

            } else {
                Log.i(TAG, "[setDiscussionRead] Document does not exist")
            }
        }
    }
}