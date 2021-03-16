package org.devnews.android.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import java.lang.IllegalStateException

/**
 * Open a browser page for a given URL using the Custom Tabs API.
 *
 * @param context Android context
 * @param url The URL to open
 */
fun openCustomTab(context: Context, url: String) {
    val customTab = CustomTabsIntent.Builder().build()
    customTab.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    customTab.launchUrl(context, Uri.parse(url))
}