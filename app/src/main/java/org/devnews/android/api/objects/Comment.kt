package org.devnews.android.api.objects

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * A comment made on a story.
 */
data class Comment(
    @SerializedName("short_url") val shortURL: String,
    @SerializedName("commented_at") val commentedAt: Date,
    val comment: String,
    @SerializedName("comment_html") val commentHtml: String,
    val username: String?,
    val score: String?,
    @SerializedName("user_voted") val userVoted: Boolean?,
    val read: Boolean?,

    val children: List<Comment>?,

    // Used for displaying comments
    val indent: Int = 0
) {
    /**
     * Return a flat list of comment where each nested level assigns the correct indent level to
     * the comment.
     */
    fun generateCommentList(indent: Int = 0): List<Comment> {
        val comments = arrayListOf(this)
        children?.forEach { child ->
            child.generateCommentList(indent + 1).forEach { comments.add(it) }
        }

        return comments
    }
}