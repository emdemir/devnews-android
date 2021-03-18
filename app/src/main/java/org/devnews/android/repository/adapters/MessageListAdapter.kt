package org.devnews.android.repository.adapters

import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.TextUtils
import android.text.format.DateUtils
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
import org.devnews.android.base.PaginatedAdapter
import org.devnews.android.repository.objects.Message
import org.devnews.android.utils.TextChanged
import java.lang.IllegalArgumentException

/**
 * Adapts Message objects to a RecyclerView.
 *
 * @param messages The messages list.
 */
class MessageListAdapter(messages: List<Message>) :
    PaginatedAdapter<Message, MessageViewHolder>(messages) {

    private var onMessageClickListener: ((Int) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    override fun createItemViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun bindItemViewHolder(holder: MessageViewHolder, position: Int) {
            // Bind messages normally.
            val message = items[position]
            holder.bindData(message, false)
            onMessageClickListener?.let { holder.setMessageClickListener(it) }
    }

    override fun getItemId(position: Int) = items[position].id.toLong()

    fun setMessageClickListener(listener: (Int) -> Unit) {
        onMessageClickListener = listener
    }
}
