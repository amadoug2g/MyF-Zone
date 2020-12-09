package mfz.myfzone_sport.myf_zone.fragments.discussion_page

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.chat.Chat
import mfz.myfzone_sport.myf_zone.model.chat.Message
import mfz.myfzone_sport.myf_zone.model.coach.ClubAffiliation
import mfz.myfzone_sport.myf_zone.model.coach.Coach
import mfz.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import mfz.myfzone_sport.myf_zone.util.Constants.DB
import java.util.*

/**
 * Created by Amadou on 07/12/2020, 12:04
 *
 * Discussion Page Service
 *
 */

object DiscussionService {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val fireStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val chatChannelsConnectionRef = fireStoreInstance.collection(COACH_PATH + "Chat")

    fun getCurrentUser() = flow<State<Coach>> {
        val userId = firebaseAuth.currentUser?.uid
        val mUserQuery = DB.document(COACH_PATH + "/${userId}")

        emit(State.loading())

        val snapshot = mUserQuery.get().await()
        val currentUser = snapshot.toObject(Coach::class.java)

        emit(State.success(currentUser!!))
    }.catch {
        emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
    }.flowOn(IO)

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

    fun getOtherUser(coachId: String) = flow<Coach> {
        val mUserQuery = DB.document(COACH_PATH + "/${coachId}")

        val snapshot = mUserQuery.get().await()
        val coach = snapshot.toObject(Coach::class.java)

        emit(coach!!)
    }

    fun getOrCreateChatChannel(
        coach: Coach,
        coachClub: ClubAffiliation,
        other: Coach,
        otherClub: ClubAffiliation
    ) {
        val time = Calendar.getInstance().time
        val mUserChatQuery = DB
            .document(COACH_PATH + "/${coach.id}/Chat/${other.id}")

        val mOtherUserChatQuery = DB
            .document(COACH_PATH + "/${other.id}/Chat/${coach.id}")

        mUserChatQuery.get().addOnSuccessListener {
            if (!it.exists()) {
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
                    coachId = coach.id
                    fullname = coach.getName()
                    clubLogo = coachClub.clubLogo
                    isTyping = false
                    lastMessage = ""
                    unread = false
                    createdDate = time
                    updatedDate = time
                }

                mUserChatQuery
//            .collection("Message").document()
                    .set(newUserChat.toMap())

                mOtherUserChatQuery
//            .collection("Message").document()
                    .set(newOtherChat.toMap())
            } else {
                Log.i("DiscussionService", "Document already exists")

                val userListMessageId = mUserChatQuery.collection("Message").document().id

                val newUseMessage = Message().apply {
                    id = userListMessageId
                    senderId = coach.id
                    senderName = coach.getName()
                    senderClubLogo = coachClub.clubLogo
                    text = "test"
                    createdDate = time
                }

                val otherListMessageId = mUserChatQuery.collection("Message").document().id

                val newMessage = Message().apply {
                    id = otherListMessageId
                    senderId = coach.id
                    senderName = coach.getName()
                    senderClubLogo = coachClub.clubLogo
                    text = "test"
                    createdDate = time
                }

//                val otherListMessage = mOtherUserChatQuery
            }
        }
    }

    fun sendChatMessage() {

    }

    fun addChatMessageListener(
        otherId: String,
        onListen: (List<Item>) -> Unit
    ): ListenerRegistration {
        val userId = firebaseAuth.currentUser?.uid

        val mUserChatQuery = DB
            .document(COACH_PATH + "/${userId}/Chat/${otherId}")

        return mUserChatQuery
            .collection("Message").orderBy("createdDate")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("DiscussionService", "Error in addChatMessageListener", error)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                value?.documents?.forEach { Log.i("DiscussionService", "item is $it") }
                value?.documents?.forEach { items.add(it.toObject()!!) }

                onListen(items)
            }
    }

}