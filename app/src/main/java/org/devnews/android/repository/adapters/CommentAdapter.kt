package org.devnews.android.repository.adapters

import android.os.Build
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.text.SpannableString
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import org.devnews.android.R
import org.devnews.android.repository.objects.Comment
import org.devnews.android.utils.dpToPx

class CommentAdapter(private var comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private var onReplyListener: ((shortURL: String) -> Unit)? = null
    private var onUpvoteListener: ((shortURL: String) -> Unit)? = null

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
    }

    override fun getItemCount() = comments.size

    fun submitList(comments: List<Comment>) {
        this.comments = comments
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return comments[position].shortURL.hashCode().toLong()
    }

    fun setOnReplyListener(listener: (shortURL: String) -> Unit) {
        onReplyListener = listener
    }

    fun setOnUpvoteListener(listener: (shortURL: String) -> Unit) {
        onUpvoteListener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val score: TextView = itemView.findViewById(R.id.score_text)
        private val byline: TextView = itemView.findViewById(R.id.comment_byline)
        private val content: TextView = itemView.findViewById(R.id.comment_content)
        private val indentIndicator: View = itemView.findViewById(R.id.indent_indicator)
        private val replyButton: Button = itemView.findViewById(R.id.reply_button)

        private var username: String? = null
        private var shortURL: String? = null

        private var onReplyListener: ((shortURL: String) -> Unit)? = null
        private var onUpvoteListener: ((shortURL: String) -> Unit)? = null

        init {
            // When the byline is clicked, spawn user details page
            byline.setOnClickListener {
                Toast.makeText(itemView.context, "TODO show user page", LENGTH_LONG).show()
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

            // Render the comment content into HTML
            val html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Html.fromHtml(comment.commentHtml, FROM_HTML_MODE_COMPACT)
            } else {
                // We're already handling the deprecation case with the SDK version check.
                @Suppress("DEPRECATION")
                Html.fromHtml(comment.commentHtml)
            }
            val spannableHtml = SpannableString(html).trim()
            content.setText(spannableHtml, TextView.BufferType.SPANNABLE)

            // Set indent indicator
            val params = indentIndicator.layoutParams as ViewGroup.MarginLayoutParams
            params.width =
                if (comment.indent > 0) dpToPx(itemView.context, 4f).toInt() else 0
            params.marginStart =
                ((comment.indent - 1) * dpToPx(itemView.context, 4f)).toInt()
            indentIndicator.layoutParams = params
        }

        fun setOnReplyListener(listener: (shortURL: String) -> Unit) {
            onReplyListener = listener
        }

        fun setOnUpvoteListener(listener: (shortURL: String) -> Unit) {
            onUpvoteListener = listener
        }
    }
}