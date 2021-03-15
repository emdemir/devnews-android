package org.devnews.android.home.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.api.*
import org.devnews.android.api.objects.Story
import org.devnews.android.base.CollectionViewModel
import org.devnews.android.base.PaginatedViewModel
import org.devnews.android.welcome.LoginViewModel
import retrofit2.HttpException
import java.lang.IllegalStateException

class HomeViewModel(
    private val indexRepository: IndexRepository,
    private val storyRepository: StoryRepository
) : PaginatedViewModel<Story>() {

    /**
     * Fetch the stories for the current page.
     */
    override suspend fun fetchData(context: Context, page: Int): List<Story>? {
        _error.value = null
        _loading.value = true

        // Load the data
        var newStories: List<Story>? = null
        val error = wrapAPIError(context) {
            newStories = indexRepository.getIndex(page)
        }

        _loading.value = false

        // Check for any errors
        return if (error != null) {
            _error.value = error
            null
        } else {
            newStories
        }
    }

    /**
     * Vote on a given story.
     *
     * @param context Activity context
     * @param shortURL The short URL for the story
     */
    fun voteOnStory(context: Context, shortURL: String) {
        _error.value = null
        val stories = _items.value!! as ArrayList<Story>

        viewModelScope.launch {
            val story = stories.find { it.shortURL == shortURL }
                ?: throw IllegalStateException("Can't find the story we just clicked on?!")
            val storyIndex = stories.indexOf(story)

            _error.value = wrapAPIError(context, {
                when (it) {
                    404 -> {
                        Log.d(TAG, "Couldn't find the story to upvote.")
                        context.getString(R.string.error_story_not_found)
                    }
                    else -> null
                }
            }) {
                storyRepository.voteOnStory(shortURL)
                val casting = story.userVoted != true
                val score = story.score

                story.score = score + if (casting) 1 else -1
                story.userVoted = casting

                // Let the view know, so that the view pass us the adapter to update.
                _updateStart.value = storyIndex
                _updateCount.value = 1
                _operation.value = OperationType.UPDATED
            }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}