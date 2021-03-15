package org.devnews.android.home.common

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import org.devnews.android.story.StoryActivity

open class StoryListFragment : Fragment() {
    /**
     * Open a browser page using the Custom Tabs API.
     *
     * @param url The URL to open
     */
    protected fun openCustomTab(url: String) {
        val customTab = CustomTabsIntent.Builder().build()
        customTab.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTab.launchUrl(requireContext(), Uri.parse(url))
    }

    /**
     * Launch a story activity with the given story short URL.
     *
     * @param shortURL The short URL of the story
     */
    protected fun launchStory(shortURL: String) {
        val context = requireContext()

        val goToStory = Intent(context, StoryActivity::class.java)
        goToStory.putExtra(StoryActivity.KEY_SHORT_URL, shortURL)
        context.startActivity(goToStory)
    }
}