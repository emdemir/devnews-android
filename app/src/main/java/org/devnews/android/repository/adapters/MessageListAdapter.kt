package org.devnews.android.repository.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import org.devnews.android.R
import org.devnews.android.base.PaginatedAdapter
import org.devnews.android.repository.objects.Message

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
