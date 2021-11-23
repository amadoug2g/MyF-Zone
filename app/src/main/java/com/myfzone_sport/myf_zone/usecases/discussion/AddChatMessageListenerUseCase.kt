package com.myfzone_sport.myf_zone.usecases.discussion

import android.content.Context
import com.myfzone_sport.myf_zone.data.Repository
import com.xwray.groupie.kotlinandroidextensions.Item

/**
 * Created by Amadou on 22/11/2021, 17:46
 */

class AddChatMessageListenerUseCase(val repository: Repository) {
    operator fun invoke(chatCoachId: String, context: Context, onListen: (List<Item>) -> Unit) =
        repository.addChatMessageListener(chatCoachId, context, onListen)
}