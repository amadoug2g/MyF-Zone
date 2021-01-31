package com.myfzone_sport.myf_zone.model.chat

import android.view.Gravity
import android.widget.FrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.myfzone_sport.myf_zone.R
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_text_message.view.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.wrapContent


/**
 * Created by Amadou on 22/12/2020
 */

abstract class MessageItem(private val message: Message) : Item() {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewMessageTime.text = message.messageDate
        setMessageGravity(viewHolder)
    }

    private fun setMessageGravity(viewHolder: GroupieViewHolder) {
        val userId = firebaseAuth.currentUser?.uid

        if (message.senderId == userId) {
            viewHolder.itemView.message_root.apply {
                backgroundResource = R.drawable.round_rect_secondary
                val lParams = FrameLayout.LayoutParams(wrapContent, wrapContent, Gravity.END)
                this.layoutParams = lParams
            }
        } else {
            viewHolder.itemView.message_root.apply {
                backgroundResource = R.drawable.round_rect_primary
                val lParams = FrameLayout.LayoutParams(wrapContent, wrapContent, Gravity.START)
                this.layoutParams = lParams
            }
        }
    }
}