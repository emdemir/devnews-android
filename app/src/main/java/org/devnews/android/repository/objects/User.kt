package org.devnews.android.repository.objects

import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.format.DateUtils
import android.util.Log
import com.google.gson.annotations.SerializedName
import org.devnews.android.utils.htmlToSpanned
import java.lang.NullPointerException
import java.util.*

data class User(
    val username: String,
    @SerializedName("registered_at") val registeredAt: Date,
    val about: String,
    @SerializedName("about_html") val aboutHtml: String,
    val homepage: String?,
    @SerializedName("avatar_image") val avatarImage: String,

    // Extra fields
    @SerializedName("comment_count") val commentCount: Int?,
    @SerializedName("story_count") val storyCount: Int?,
    val karma: Int?
) {
    /**
     * Return a timespan string from the registration date of this user until now.
     */
    fun getRegisteredTimeSpan(): CharSequence {
        return try {
            val now = System.currentTimeMillis()

            DateUtils.getRelativeTimeSpanString(
                registeredAt.time,
                now,
                DateUtils.MINUTE_IN_MILLIS
            )
        } catch (e: NullPointerException) {
            // The data-binding library will "correctly" initialize a User object with nulls in each
            // property. However calling functions means we don't get any null protection. As Kotlin
            // believes the data class' values are non-nullable (and they really are, the
            // ViewBindingImpl class is just using Java and can circumvent that protection), we can't
            // put a (== null) check either. So the best course of action is just catching NPEs and
            // returning a default value.
            ""
        }
    }

    /**
     * Format the about HTML into a span and return it.
     */
    fun getAboutSpanned(): CharSequence {
        return try {
            htmlToSpanned(aboutHtml)
        } catch (e: NullPointerException) {
            // Same business as above.
            ""
        }
    }
}