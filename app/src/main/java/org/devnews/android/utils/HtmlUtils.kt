package org.devnews.android.utils

import android.os.Build
import android.text.Html
import android.text.SpannableString

/**
 * Convert an HTML string into a spanned character sequence.
 *
 * @param text The original HTML
 */
fun htmlToSpanned(text: String): CharSequence {
    val html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
    } else {
        // We're already handling the deprecation case with the SDK version check.
        @Suppress("DEPRECATION")
        Html.fromHtml(text)
    }

    return SpannableString(html).trim()
}