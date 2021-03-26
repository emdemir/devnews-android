package org.devnews.android.repository.objects

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.ArrayList

/**
 * A comment made on a story.
 */
data class Comment(
    @SerializedName("short_url") val shortURL: String,
    @SerializedName("commented_at") val commentedAt: Date,
    val comment: String,
    @SerializedName("comment_html") val commentHtml: String,
    var score: Int,
    val username: String?,
    @SerializedName("user_voted") var userVoted: Boolean?,
    @SerializedName("user_read") val userRead: Boolean?,
    @SerializedName("story_url") val storyURL: String?,

    var children: ArrayList<Comment>?,

    // Used for displaying comments
    var indent: Int = 0
) {
    /**
     * Toggle the user's vote on this comment, and update the score.
     */
    fun toggleVote() {
        val voted = userVoted == true

        score += if (voted) -1 else 1
        userVoted = !voted
    }

    private data class CommentListIterator(val list: List<Comment>, var index: Int = 0)
    companion object {
        /**
         * Return a flat list of comment where each nested level assigns the correct indent level to
         * the comment.
         */
        fun generateCommentList(comments: List<Comment>): List<Comment> {
            // This is an iterative tree flattening operation.
            var currentIterator = CommentListIterator(comments)
            val stack = Stack<CommentListIterator>()
            val list = ArrayList<Comment>()

            stack.push(currentIterator)

            do {
                while (currentIterator.index < currentIterator.list.size) {
                    val comment = currentIterator.list[currentIterator.index]
                    comment.indent = stack.size - 1
                    list.add(comment)
                    currentIterator.index++

                    val children = comment.children
                    if (children != null && children.size > 0) {
                        stack.push(currentIterator)
                        currentIterator = CommentListIterator(children, 0)
                    }
                }

                currentIterator = stack.pop()
            } while (stack.size > 0)

            return list
        }

    }
}