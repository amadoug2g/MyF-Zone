package com.myfzone_sport.myf_zone.screens

import androidx.lifecycle.ViewModel
import com.myfzone_sport.myf_zone.domain.chat.Chat


/**
 * Created by Amadou on 17/12/2020
 *
 * Main ViewModel class
 *
 */


class MainViewModel : ViewModel() {
    fun addChatListener(onListen: (MutableList<Chat>) -> Unit) =
        MainService.addChatListener(onListen)
}