package org.devnews.android.repository.adapters

import android.text.format.DateUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import org.devnews.android.R
import org.devnews.android.repository.objects.Comment
import org.devnews.android.utils.dpToPx
import org.devnews.android.utils.htmlToSpanned

class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private var onReplyListener: ((shortURL: String) -> Unit)? = null
    private var onUpvoteListener: ((shortURL: String) -> Unit)? = null
    private var onUsernameClickListener: ((username: String) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.bindData(comment)
        onReplyListener?.let { holder.setOnReplyListener(it) }
        onUpvoteListener?.let { holder.setOnUpvoteListener(it) }
        onUsernameClickListener?.let { holder.setOnUsernameClickListener(it) }
    }

    override fun getItemCount() = comments.size

    override fun getItemId(position: Int): Long {
        return comments[position].shortURL.hashCode().toLong()
    }

    fun setOnReplyListener(listener: (shortURL: String) -> Unit) {
        onReplyListener = listener
    }

    fun setOnUpvoteListener(listener: (shortURL: String) -> Unit) {
        onUpvoteListener = listener
    }

    fun setOnUsernameClickListener(listener: (username: String) -> Unit) {
        onUsernameClickListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val score: TextView = itemView.findViewById(R.id.score_text)
        private val byline: TextView = itemView.findViewById(R.id.comment_byline)
        private val unread: TextView = itemView.findViewById(R.id.comment_unread)
        private val content: TextView = itemView.findViewById(R.id.comment_content)
        private val indentIndicator: View = itemView.findViewById(R.id.indent_indicator)
        private val replyButton: Button = itemView.findViewById(R.id.reply_button)

        private var username: String? = null
        private var shortURL: String? = null

        private var onReplyListener: ((shortURL: String) -> Unit)? = null
        private var onUpvoteListener: ((shortURL: String) -> Unit)? = null
        private var onUsernameClickListener: ((username: String) -> Unit)? = null

        init {
            // When the byline is clicked, spawn user details page
            byline.setOnClickListener {
                username?.let {
                    onUsernameClickListener?.invoke(it)
                }
            }

            // When the reply button is clicked, if there is a reply listener, call it.
            replyButton.setOnClickListener {
                shortURL?.let {
                    onReplyListener?.invoke(it)
                }
            }

            // When the upvote button is clicked, if there is an upvote listener, call it.
            score.setOnClickListener {
                shortURL?.let {
                    onUpvoteListener?.invoke(it)
                }
            }
        }

        fun bindData(comment: Comment) {
            username = comment.username
            shortURL = comment.shortURL

            score.text = comment.score.toString()
            // If the user has voted on this, then highlight the upvote button, otherwise set it
            // to the regular text color.
            val textColor = TypedValue()
            itemView.context.theme.resolveAttribute(R.attr.colorOnBackground, textColor, true)
            if (comment.userVoted == true) {
                score.setTextColor(itemView.context.getColor(R.color.upvoteYellow))
                TextViewCompat.setCompoundDrawableTintList(
                    score,
                    ContextCompat.getColorStateList(itemView.context, R.color.upvoteYellow)
                )
            } else {
                score.setTextColor(textColor.data)
                TextViewCompat.setCompoundDrawableTintList(score, null)
            }

            val createdAt = comment.commentedAt.time
            val now = System.currentTimeMillis()
            val span = DateUtils.getRelativeTimeSpanString(
                createdAt, now,
                DateUtils.MINUTE_IN_MILLIS
            )
            byline.text = itemView.context.getString(R.string.byline, span, comment.username)

            unread.visibility = if (comment.userRead == false) VISIBLE else GONE

            content.setText(htmlToSpanned(comment.commentHtml), TextView.BufferType.SPANNABLE)

            // Set indent indicator
            val params = indentIndicator.layoutParams as ViewGroup.MarginLayoutParams
            params.width =
                if (comment.indent > 0) dpToPx(itemView.context, 4f) else 0
            params.marginStart =
                ((comment.indent - 1) * dpToPx(itemView.context, 4f))
            indentIndicator.layoutParams = params
        }

        fun setOnReplyListener(listener: (shortURL: String) -> Unit) {
            onReplyListener = listener
        }

        fun setOnUpvoteListener(listener: (shortURL: String) -> Unit) {
            onUpvoteListener = listener
        }

        fun setOnUsernameClickListener(listener: (username: String) -> Unit) {
            onUsernameClickListener = listener
        }
    }
}