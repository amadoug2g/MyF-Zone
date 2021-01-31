package com.myfzone_sport.myf_zone.model.chat

import android.content.Context
import com.myfzone_sport.myf_zone.R
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_text_message.view.*

/**
 * Created by Amadou on 07/12/2020, 18:24
 *
 * : holds the chat text messages in Discussion Page
 *
 */

class TextMessageItem(val message: Message, val context: Context) : MessageItem(message) {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewMessageText.text = message.text
        super.bind(viewHolder, position)
    }

    override fun isSameAs(other: com.xwray.groupie.Item<*>): Boolean {
        if (other !is TextMessageItem)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs((other as? TextMessageItem)!!)
    }

    override fun getLayout() = R.layout.item_text_message
    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}