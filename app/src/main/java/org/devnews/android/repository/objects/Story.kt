package org.devnews.android.repository.objects

import com.google.gson.annotations.SerializedName
import java.net.URL
import java.util.*

/**
 * Represents a story in DevNews.
 */
data class Story(
    @SerializedName("short_url") val shortURL: String,
    val title: String,
    val url: String?,
    val text: String?,
    @SerializedName("text_html") val textHtml: String?,
    @SerializedName("submitted_at") val submittedAt: Date,
    @SerializedName("submitter_username") val submitterUsername: String?,
    var score: Int,
    @SerializedName("comment_count") var commentCount: Int,
    @SerializedName("user_voted") var userVoted: Boolean?,

    val tags: List<String>?,
    val comments: List<Comment>?
) {
    // The domain part of the URL. null if this isn't a link story.
    val domain: String?
        get() {
            return if (url != null) {
                URL(url).host
            } else {
                null
            }
        }

    /**
     * Toggle the user's vote on this story, and update the score.
     */
    fun toggleVote() {
        val voted = userVoted == true

        score += if (voted) -1 else 1
        userVoted = !voted
    }
}