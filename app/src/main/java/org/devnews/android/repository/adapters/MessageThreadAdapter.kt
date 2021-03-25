package org.devnews.android.repository.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import org.devnews.android.R
import org.devnews.android.repository.objects.Message
import org.devnews.android.utils.TextChanged

/**
 * Adapts Message objects to a RecyclerView.
 *
 * @param messages The messages list.
 */
class MessageThreadAdapter(private val messages: List<Message>) :
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
        if (position == messages.size) {
            // Bind the reply box.
            val holder = _holder as ReplyViewHolder
            onReplyListener?.let { holder.setReplyListener(it) }
        } else {
            // Bind messages normally.
            val holder = _holder as MessageViewHolder
            val message = messages[position]
            holder.bindData(message, true)
            onMessageClickListener?.let { holder.setMessageClickListener(it) }
        }
    }

    override fun getItemCount() = messages.size + 1

    override fun getItemId(position: Int): Long {
        return if (position == messages.size) {
            0 // Messages will never get ID 0
        } else {
            messages[position].id.toLong()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == messages.size) {
            TYPE_REPLY
        } else {
            TYPE_MESSAGE
        }
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
                        val adapter = it as MessageThreadAdapter
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
