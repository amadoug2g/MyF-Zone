package mfz.myfzone_sport.myf_zone.model.chat

/**
 * Created by Amadou on 07/12/2020, 18:33
 *
 * Chat Channel class
 *
 */

data class ChatChannel(val userIds: MutableList<String>) {
    constructor() : this(mutableListOf())
}