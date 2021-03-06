package org.devnews.android.api.objects

import com.google.gson.annotations.SerializedName

/**
 * Represents a story in DevNews.
 */
data class Story(
    @SerializedName("short_url") val shortURL: String,
    val title: String,
    val url: String?,
    val text: String?,
    @SerializedName("submitter_username") val submitterUsername: String?,
    val score: Int,
    @SerializedName("comment_count") val commentCount: Int,
    @SerializedName("user_voted") val userVoted: Boolean?,

    val tags: List<String>?,
    val comments: List<Comment>?
)