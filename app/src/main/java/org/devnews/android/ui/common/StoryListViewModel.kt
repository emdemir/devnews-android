package org.devnews.android.ui.common

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.base.PaginatedViewModel
import org.devnews.android.repository.StoryRepository
import org.devnews.android.repository.objects.Story
import org.devnews.android.repository.wrapAPIError
import java.lang.IllegalArgumentException

/**
 * Groups together common operations done with ViewModels of a list of stories.
 */
abstract class StoryListViewModel(private val storyRepository: StoryRepository) :
    PaginatedViewModel<Story>() {
    /**
     * Either cast or retract a vote on a story.
     *
     * @param context Android context
     * @param shortURL The short URL for the story
     */
    fun voteOnStory(context: Context, shortURL: String) {
        // I really wanted to avoid repeating this code between the StoryViewModel and the
        // story list ViewModels, however I have not been able to find any clean way of doing it.
        val stories = _items.value!! as ArrayList<Story>
        val storyIndex = stories.indexOfFirst { it.shortURL == shortURL }
        if (storyIndex == -1)
            throw IllegalArgumentException("Non-existent short URL passed")
        val story = stories[storyIndex]

        viewModelScope.launch {
            _error.value = wrapAPIError(context, {
                when (it) {
                    404 -> {
                        Log.d(TAG, "Could not find the story to vote on.")
                        context.getString(R.string.error_story_not_found)
                    }
                    else -> null
                }
            }) {
                storyRepository.voteOnStory(story.shortURL)
                story.toggleVote()
                collectionChanged(storyIndex, 1, OperationType.UPDATED)
            }
        }
    }

    companion object {
        private const val TAG = "StoryListViewModel"
    }
}