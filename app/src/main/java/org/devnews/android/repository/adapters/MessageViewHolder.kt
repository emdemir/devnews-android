package org.devnews.android.repository.adapters

import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.devnews.android.R
import org.devnews.android.repository.objects.Message
import org.devnews.android.utils.htmlToSpanned

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val senderRecipient: TextView = itemView.findViewById(R.id.message_sender_recipient)
    private val date: TextView = itemView.findViewById(R.id.message_date)
    private val content: TextView = itemView.findViewById(R.id.message_content)

    private var onMessageClickListener: ((Int) -> Unit)? = null

    private var messageID: Int? = null

    init {
        itemView.setOnClickListener {
            messageID?.let {
                onMessageClickListener?.invoke(it)
            }
        }
    }

    fun bindData(message: Message, thread: Boolean) {
        messageID = message.id

        senderRecipient.text = itemView.context.getString(
            R.string.sender_to_recipient,
            message.author,
            message.recipient
        )

        // Get time span for the message creation
        val createdAt = message.sentAt.time
        val now = System.currentTimeMillis()
        date.text = DateUtils.getRelativeTimeSpanString(
            createdAt, now,
            DateUtils.MINUTE_IN_MILLIS
        )

        content.setText(htmlToSpanned(message.messageHtml), TextView.BufferType.SPANNABLE)

        // If we are not in thread mode, then make the text larger and ellipsize it.
        if (!thread) {
            content.setTextAppearance(R.style.ThemeOverlay_DevNews_TextAppearance_Large)
            content.ellipsize = TextUtils.TruncateAt.END
            content.isSingleLine = true
            content.setTextIsSelectable(false)
        } else {
            content.setTextAppearance(R.style.ThemeOverlay_DevNews_TextAppearance_Medium)
            content.ellipsize = null
            content.isSingleLine = false
            content.setTextIsSelectable(true)
        }
    }

    fun setMessageClickListener(listener: (Int) -> Unit) {
        onMessageClickListener = listener
    }
}