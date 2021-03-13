package org.devnews.android.api.adapters

import android.os.Build
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.SpannableString
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.RecyclerView
import org.devnews.android.R
import org.devnews.android.api.objects.Comment

class CommentAdapter(private var comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.bindData(comment)
    }

    override fun getItemCount() = comments.size

    fun submitList(comments: List<Comment>) {
        this.comments = comments
        notifyDataSetChanged()
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val score: TextView = itemView.findViewById(R.id.score_text)
        private val byline: TextView = itemView.findViewById(R.id.comment_byline)
        private val content: TextView = itemView.findViewById(R.id.comment_content)

        private var username: String? = null

        init {
            // When the byline is clicked, spawn user details page
            byline.setOnClickListener {
                Toast.makeText(itemView.context, "TODO show user page", LENGTH_LONG).show()
            }
        }

        fun bindData(comment: Comment) {
            username = comment.username

            score.text = comment.score.toString()

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
        }
    }
}