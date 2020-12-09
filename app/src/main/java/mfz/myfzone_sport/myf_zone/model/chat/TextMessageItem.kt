package mfz.myfzone_sport.myf_zone.model.chat

import android.content.Context
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import mfz.myfzone_sport.myf_zone.R

/**
 * Created by Amadou on 07/12/2020, 18:24
 *
 * : holds the chat messages in Discussions
 *
 */

class TextMessageItem(val message: Message, val context: Context) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getLayout() = R.layout.item_text_message
}