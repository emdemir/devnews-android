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
    val username: String?,
    val score: String?,
    @SerializedName("user_voted") val userVoted: Boolean?,
    val read: Boolean?
)
