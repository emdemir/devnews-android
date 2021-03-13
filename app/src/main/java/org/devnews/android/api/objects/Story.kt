package org.devnews.android.api.objects

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
    @SerializedName("submitted_at") val submittedAt: Date,
    @SerializedName("submitter_username") val submitterUsername: String?,
    val score: Int,
    @SerializedName("comment_count") val commentCount: Int,
    @SerializedName("user_voted") val userVoted: Boolean?,

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
}