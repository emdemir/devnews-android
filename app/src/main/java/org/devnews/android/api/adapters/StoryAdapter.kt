package org.devnews.android.api.adapters

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.text.TextUtils
import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.devnews.android.R
import org.devnews.android.api.objects.Story
import org.devnews.android.story.StoryActivity


class StoryAdapter(
    private var stories: List<Story>
) : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.story_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = stories[position]
        holder.bindData(story)
    }

    override fun getItemCount() = stories.size

    fun submitList(stories: List<Story>) {
        this.stories = stories
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val score: TextView = itemView.findViewById(R.id.score_text)
        private val commentCount: TextView = itemView.findViewById(R.id.comment_count_text)
        private val title: TextView = itemView.findViewById(R.id.title_text)
        private val domain: Chip = itemView.findViewById(R.id.domain_chip)
        private val byline: TextView = itemView.findViewById(R.id.byline_text)
        private val tagGroup: ChipGroup = itemView.findViewById(R.id.tag_group)

        private val storyDetails: LinearLayout = itemView.findViewById(R.id.story_details)
        var url: String? = ""
        var shortURL: String? = null
        var enableStoryListener = true

        init {
            storyDetails.setOnClickListener {
                if (!TextUtils.isEmpty(url)) {
                    val context = itemView.context

                    val customTab = CustomTabsIntent.Builder().build()
                    customTab.intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    customTab.launchUrl(context, Uri.parse(url))
                }
            }

            // If the user clicks the comment count, send the user to the story
            commentCount.setOnClickListener {
                if (!enableStoryListener) return@setOnClickListener
                val url = shortURL ?: return@setOnClickListener

                val goToStory = Intent(itemView.context, StoryActivity::class.java)
                goToStory.putExtra(StoryActivity.KEY_SHORT_URL, url)
                itemView.context.startActivity(goToStory)
            }
        }

        /**
         * Bind the story to the current view.
         *
         * @param story The story that's being bound.
         * @param enableStoryListener If true, then clicking the comment count will launch story
         * details. Otherwise nothing will happen.
         */
        fun bindData(story: Story, enableStoryListener: Boolean = true) {
            shortURL = story.shortURL
            url = story.url
            this.enableStoryListener = enableStoryListener

            score.text = story.score.toString()
            commentCount.text = story.commentCount.toString()
            title.text = story.title

            if (story.domain != null) {
                domain.visibility = VISIBLE
                domain.text = story.domain
            } else {
                domain.visibility = GONE
            }

            val createdAt = story.submittedAt.time
            val now = System.currentTimeMillis()

            val span = DateUtils.getRelativeTimeSpanString(createdAt, now, MINUTE_IN_MILLIS)

            byline.text =
                itemView.context.getString(R.string.byline, span, story.submitterUsername)

            // Clear and add tags
            tagGroup.removeAllViews()
            story.tags?.let {
                for (tag in it) {
                    val chip = Chip(itemView.context)
                    chip.text = tag
                    chip.ensureAccessibleTouchTarget(0)
                    chip.shapeAppearanceModel = chip.shapeAppearanceModel.withCornerSize(
                        // Calculate 6dp
                        6.0F * itemView.context.resources.displayMetrics.density
                    )
                    tagGroup.addView(chip)
                }
            }
        }
    }
}