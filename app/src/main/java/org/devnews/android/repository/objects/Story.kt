package org.devnews.android.repository.objects

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.google.gson.annotations.SerializedName
import java.lang.IllegalStateException
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
     * Open a browser page for this story using the Custom Tabs API.
     *
     * @param context Android context
     */
    fun openCustomTab(context: Context) {
        if (url == null)
            throw IllegalStateException("You're trying to open custom tab with a text story!")

        val customTab = CustomTabsIntent.Builder().build()
        customTab.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTab.launchUrl(context, Uri.parse(url))
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