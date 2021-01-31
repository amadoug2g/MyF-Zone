package com.myfzone_sport.myf_zone.model.chat

import android.content.Context
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.fragments.profile.ProfileService
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_image_message.view.*

/**
 * Created by Amadou on 22/12/2020
 *
 * : holds the chat image messages in Discussion Page
 *
 */

class ImageMessageItem(val message: Message, val context: Context) : MessageItem(message) {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        super.bind(viewHolder, position)
        GlideApp.with(context).apply {
            load(ProfileService.getImageReference(message.image!!))
                .centerCrop()
                .placeholder(R.drawable.ic_image)
                .into(viewHolder.itemView.imageViewMessageText)
        }
    }

    override fun isSameAs(other: com.xwray.groupie.Item<*>): Boolean {
        if (other !is ImageMessageItem)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs((other as? ImageMessageItem)!!)
    }

    override fun getLayout() = R.layout.item_image_message
    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}