package mfz.myfzone_sport.myf_zone.model.chat

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_text_message.view.*
import mfz.myfzone_sport.myf_zone.R
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.wrapContent

/**
 * Created by Amadou on 07/12/2020, 18:24
 *
 * : holds the chat messages in Discussion Page
 *
 */

class TextMessageItem(val message: Message, val context: Context) : Item() {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textViewMessageText.text = message.text
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
}