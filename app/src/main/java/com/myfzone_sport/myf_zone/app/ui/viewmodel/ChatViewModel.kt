package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.StorageReference
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.chat.Message
import com.myfzone_sport.myf_zone.domain.chat.TextMessageItem
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.usecases.discussion.*
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.util.Constants.COACH_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 22/11/2021, 14:31
 */

class ChatViewModel(
    private val getChatCoachUseCase: GetChatCoachUseCase,
    private val getChatCoachClubUseCase: GetChatCoachClubUseCase,
    private val getOrCreateChatUseCase: GetOrCreateChatUseCase,
    private val createChatUseCase: CreateChatUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val setDiscussionReadUseCase: SetDiscussionReadUseCase,
    private val setDiscussionUnreadUseCase: SetDiscussionUnreadUseCase,
    private val addChatMessageListenerUseCase: AddChatMessageListenerUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
) : ViewModel() {

    //region Variables
    private val _chatCoach = MutableLiveData<Coach>()
    val chatCoach: LiveData<Coach> = _chatCoach

    private val _chatCoachAffiliation = MutableLiveData<ClubAffiliation>()
    val chatCoachAffiliation: LiveData<ClubAffiliation> = _chatCoachAffiliation

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    fun assignChatCoach(chatCoachId: String) {
        getChatCoach(chatCoachId)
        getChatCoachClub(chatCoachId)
    }

    private fun getChatCoach(chatCoachId: String) {
        viewModelScope.launch {
            getChatCoachUseCase.invoke(chatCoachId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val coach = state.data

                        _chatCoach.postValue(coach)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Chat coach update failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    private fun getChatCoachClub(chatCoachId: String) {
        viewModelScope.launch {
            getChatCoachClubUseCase.invoke(chatCoachId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val affiliation = state.data

                        _chatCoachAffiliation.postValue(affiliation)
                        onResult()
                    }
                    is State.Failed -> {
                        val message = "Chat club update failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun sendChatMessage(chatCoach: Coach, message: String, photo: String) {
        sendChatMessageUseCase.invoke(chatCoach, message, photo)
    }

    fun addChatListener(chatCoachId: String, context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration? {
        return addChatMessageListenerUseCase.invoke(chatCoachId, context, onListen)
    }

    fun addChatMessageListener(
        otherId: String,
        context: Context,
        onListen: (List<Item>) -> Unit
    ): ListenerRegistration? {
//        val userId = firebaseAuth.currentUser?.uid

        val mUserChatQuery = DB
            .document(COACH_PATH + "/${activeCoach?.id}/Chat/${otherId}")
        return try {
            mUserChatQuery
                .collection("/Message").orderBy("createdDate")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e("tagging", "Error in addChatMessageListener", error)
                        return@addSnapshotListener
                    }

                    val items = mutableListOf<Item>()
                    value?.documents?.forEach {
                        try {
                            items.add(TextMessageItem(it.toObject(Message::class.java)!!, context))
                        } catch (e: Exception) {
                            Log.i("tagging", "Error when fetching messages: $e")
                        }
                    }

                    onListen(items)
                }
        } catch (e: Exception) {
//            Log.e(TAG, "Error in addChatMessageListener: ${e.localizedMessage}")
            null
        }
    }

    fun createChat(chatCoach: Coach, chatCoachClub: ClubAffiliation) {
        createChatUseCase.invoke(chatCoach, chatCoachClub)
    }

    fun getOrCreateChat(
        chatCoach: Coach,
        chatCoachClub: ClubAffiliation,
        message: String,
        photo: String
    ) {
        getOrCreateChatUseCase.invoke(chatCoach, chatCoachClub, message, photo)
    }

    fun setChatRead(chatCoach: Coach) {
        setDiscussionReadUseCase.invoke(chatCoach)
    }

    fun setChatUnread(chatCoach: Coach) {
        setDiscussionUnreadUseCase.invoke(chatCoach)
    }

    fun getImageReference(path: String): StorageReference {
        return getImageReferenceUseCase.invoke(path)
    }
    //endregion

    //region Observers
    private fun onResult(message: String = "") {
        _errorMessage.postValue(message)
        stopLoading()
        resetErrorMsg()
    }

    private fun resetErrorMsg() {
        _errorMessage.postValue("")
    }

    private fun startLoading() {
        _isLoading.postValue(true)
    }

    private fun stopLoading() {
        _isLoading.postValue(false)
    }
    //endregion
}

class ChatViewModelFactory(
    private val getChatCoachUseCase: GetChatCoachUseCase,
    private val getChatCoachClubUseCase: GetChatCoachClubUseCase,
    private val getOrCreateChatUseCase: GetOrCreateChatUseCase,
    private val createChatUseCase: CreateChatUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val setDiscussionReadUseCase: SetDiscussionReadUseCase,
    private val setDiscussionUnreadUseCase: SetDiscussionUnreadUseCase,
    private val addChatMessageListenerUseCase: AddChatMessageListenerUseCase,
    private val getImageReferenceUseCase: GetImageReferenceUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetChatCoachUseCase::class.java,
            GetChatCoachClubUseCase::class.java,
            GetOrCreateChatUseCase::class.java,
            CreateChatUseCase::class.java,
            SendChatMessageUseCase::class.java,
            SetDiscussionReadUseCase::class.java,
            SetDiscussionUnreadUseCase::class.java,
            AddChatMessageListenerUseCase::class.java,
            GetImageReferenceUseCase::class.java,
        )
            .newInstance(
                getChatCoachUseCase,
                getChatCoachClubUseCase,
                getOrCreateChatUseCase,
                createChatUseCase,
                sendChatMessageUseCase,
                setDiscussionReadUseCase,
                setDiscussionUnreadUseCase,
                addChatMessageListenerUseCase,
                getImageReferenceUseCase,
            )
    }
}