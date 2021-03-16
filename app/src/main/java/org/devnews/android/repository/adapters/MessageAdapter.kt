package org.devnews.android.repository.adapters

import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import org.devnews.android.R
import org.devnews.android.repository.objects.Message
import org.devnews.android.utils.TextChanged
import java.lang.IllegalArgumentException

/**
 * Adapts Message objects to a RecyclerView.
 *
 * @param messages The messages list.
 * @param thread Thread mode enables a reply box at the bottom and smaller text. If false then
 * larger text is used.
 */
class MessageAdapter(private val messages: List<Message>, private val thread: Boolean) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var replyDoneCallback: (() -> Unit)? = null
    private var onMessageClickListener: ((Int) -> Unit)? = null
    private var onReplyListener: ((String) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MESSAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_message, parent, false)
                MessageViewHolder(view)
            }
            TYPE_REPLY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_message_reply, parent, false)
                ReplyViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown viewType.")
        }
    }

    override fun onBindViewHolder(_holder: RecyclerView.ViewHolder, position: Int) {
        if (thread && position == messages.size) {
            // Bind the reply box.
            val holder = _holder as ReplyViewHolder
            onReplyListener?.let { holder.setReplyListener(it) }
        } else {
            // Bind messages normally.
            val holder = _holder as MessageViewHolder
            val message = messages[position]
            holder.bindData(message, thread)
            onMessageClickListener?.let { holder.setMessageClickListener(it) }
        }
    }

    override fun getItemCount() = if (thread) messages.size + 1 else messages.size

    override fun getItemId(position: Int): Long {
        return if (thread && position == messages.size) {
            0 // Messages will never get ID 0
        } else {
            messages[position].id.toLong()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (thread && position == messages.size) {
            TYPE_REPLY
        } else {
            TYPE_MESSAGE
        }
    }

    fun setMessageClickListener(listener: (Int) -> Unit) {
        onMessageClickListener = listener
    }

    fun setReplyListener(listener: (String) -> Unit) {
        onReplyListener = listener
    }

    // Called by the ViewHolder.
    private fun setReplyDoneCallback(callback: () -> Unit) {
        replyDoneCallback = callback
    }

    /**
     * Notify the ViewHolder that the reply operation has been completed.
     */
    fun notifyReplyDone() {
        replyDoneCallback?.invoke()
    }

    /**
     * The ViewHolder subclass for regular messages.
     */
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

            // Render the message content into HTML
            val html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Html.fromHtml(message.messageHtml, Html.FROM_HTML_MODE_COMPACT)
            } else {
                // We're already handling the deprecation case with the SDK version check.
                @Suppress("DEPRECATION")
                Html.fromHtml(message.messageHtml)
            }
            val spannableHtml = SpannableString(html).trim()
            content.setText(spannableHtml, TextView.BufferType.SPANNABLE)

            // If we are not in thread mode, then make the text larger and ellipsize it.
            if (!thread) {
                content.setTextAppearance(R.style.TextAppearance_AppCompat_Large)
                content.ellipsize = TextUtils.TruncateAt.END
                content.setTextIsSelectable(false)
            } else {
                content.setTextAppearance(R.style.TextAppearance_AppCompat_Medium)
                content.ellipsize = null
                content.setTextIsSelectable(true)
            }
        }

        fun setMessageClickListener(listener: (Int) -> Unit) {
            onMessageClickListener = listener
        }
    }

    /**
     * The ViewHolder subclass for the reply box.
     */
    class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextInputLayout = itemView.findViewById(R.id.message_content)
        private val sendButton: ImageView = itemView.findViewById(R.id.send_button)
        private val progress: ProgressBar = itemView.findViewById(R.id.progress)

        private var onReplyListener: ((String) -> Unit)? = null

        init {
            // We must always stay in memory, so the reference to the ViewHolder (for reply callback)
            // is always valid.
            setIsRecyclable(false)

            // Validate the contents of the message when text is entered.
            contentText.editText!!.addTextChangedListener(TextChanged { validate() })

            // When send button is clicked, let the reply listener know.
            sendButton.setOnClickListener {
                if (validate()) {
                    sendButton.isClickable = false
                    sendButton.visibility = INVISIBLE
                    progress.visibility = VISIBLE
                    contentText.isEnabled = false

                    // Set the reply callback on the adapter binding us, so the view can notify
                    // us that the reply operation finished (success or otherwise).
                    bindingAdapter?.let {
                        val adapter = it as MessageAdapter
                        adapter.setReplyDoneCallback {
                            // Clear and remove error
                            contentText.editText!!.setText("")
                            contentText.isErrorEnabled = false
                            // Hide the progress bar
                            sendButton.visibility = VISIBLE
                            progress.visibility = GONE
                            // Re-enable components
                            sendButton.isClickable = true
                            contentText.isEnabled = true
                        }
                    }

                    onReplyListener?.invoke(contentText.editText!!.text.toString())
                }
            }
        }

        fun setReplyListener(listener: (String) -> Unit) {
            onReplyListener = listener
        }

        private fun validate(): Boolean {
            return when {
                TextUtils.isEmpty(contentText.editText!!.text) -> {
                    contentText.error = itemView.context.getString(R.string.validate_content_empty)
                    false
                }
                else -> {
                    contentText.error = null
                    true
                }
            }
        }
    }

    companion object {
        const val TYPE_MESSAGE = 1
        const val TYPE_REPLY = 2
    }
}
