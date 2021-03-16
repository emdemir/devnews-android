package org.devnews.android.repository.adapters

import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.devnews.android.R
import org.devnews.android.repository.objects.Story
import org.devnews.android.utils.dpToPx
import java.lang.IllegalStateException


class StoryAdapter(
    private var stories: List<Story>
) : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    enum class StoryType { URL, TEXT }

    private var onUpvoteClickListener: ((String) -> Unit)? = null
    private var onCommentsClickListener: ((String) -> Unit)? = null
    private var onDetailsClickListener: ((String, StoryType) -> Unit)? = null
    private var onTagClickListener: ((String) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_story, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = stories[position]
        holder.bindData(story)
        onUpvoteClickListener?.let { holder.setUpvoteClickListener(it) }
        onCommentsClickListener?.let { holder.setCommentsClickListener(it) }
        onDetailsClickListener?.let { holder.setDetailsClickListener(it) }
        onTagClickListener?.let { holder.setTagClickListener(it) }
    }

    override fun getItemCount() = stories.size

    fun submitList(stories: List<Story>) {
        this.stories = stories
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return stories[position].hashCode().toLong()
    }

    fun setUpvoteClickListener(listener: (String) -> Unit) {
        onUpvoteClickListener = listener
    }

    fun setCommentsClickListener(listener: (String) -> Unit) {
        onCommentsClickListener = listener
    }

    fun setDetailsClickListener(listener: (String, StoryType) -> Unit) {
        onDetailsClickListener = listener
    }

    fun setTagClickListener(listener: (String) -> Unit) {
        onTagClickListener = listener
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

        private var onUpvoteClickListener: ((String) -> Unit)? = null
        private var onCommentsClickListener: ((String) -> Unit)? = null
        private var onDetailsClickListener: ((String, StoryType) -> Unit)? = null
        private var onTagClickListener: ((String) -> Unit)? = null

        init {
            storyDetails.setOnClickListener {
                val url = url
                val shortURL =
                    shortURL ?: throw IllegalStateException("ShortURL is null for story item?!")

                if (url == null) {
                    onDetailsClickListener?.invoke(shortURL, StoryType.TEXT)
                } else {
                    onDetailsClickListener?.invoke(url, StoryType.URL)
                }
            }

            commentCount.setOnClickListener {
                shortURL?.let {
                    onCommentsClickListener?.invoke(it)
                }
            }

            score.setOnClickListener {
                shortURL?.let {
                    onUpvoteClickListener?.invoke(it)
                }
            }
        }

        /**
         * Bind the story to the current view.
         *
         * @param story The story that's being bound.
         */
        fun bindData(story: Story) {
            shortURL = story.shortURL
            url = story.url

            score.text = story.score.toString()
            commentCount.text = story.commentCount.toString()
            title.text = story.title

            // If the story was upvoted by us then show it as orange.
            val textColor = TypedValue()
            itemView.context.theme.resolveAttribute(R.attr.colorOnBackground, textColor, true)
            if (story.userVoted == true) {
                score.setTextColor(itemView.context.getColor(R.color.upvoteYellow))
                TextViewCompat.setCompoundDrawableTintList(
                    score,
                    ContextCompat.getColorStateList(itemView.context, R.color.upvoteYellow)
                )
            } else {
                score.setTextColor(textColor.data)
                TextViewCompat.setCompoundDrawableTintList(score, null)
            }

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
                    chip.tag = tag
                    chip.text = tag
                    chip.ensureAccessibleTouchTarget(0)
                    chip.shapeAppearanceModel = chip.shapeAppearanceModel.withCornerSize(
                        dpToPx(itemView.context, 6f)
                    )
                    tagGroup.addView(chip)

                    chip.setOnClickListener {
                        onTagClickListener?.invoke(chip.tag as String)
                    }
                }
            }
        }

        fun setUpvoteClickListener(listener: (String) -> Unit) {
            onUpvoteClickListener = listener
        }

        fun setCommentsClickListener(listener: (String) -> Unit) {
            onCommentsClickListener = listener
        }

        fun setDetailsClickListener(listener: (String, StoryType) -> Unit) {
            onDetailsClickListener = listener
        }

        fun setTagClickListener(listener: (String) -> Unit) {
            onTagClickListener = listener
        }
    }

    companion object {
        private const val TAG = "StoryAdapter"
    }
}