package org.devnews.android.ui.home.comments

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.base.PaginatedViewModel
import org.devnews.android.repository.CommentRepository
import org.devnews.android.repository.IndexRepository
import org.devnews.android.repository.IndexService
import org.devnews.android.repository.objects.Comment
import org.devnews.android.repository.wrapAPIError
import org.devnews.android.ui.story.details.StoryDetailsViewModel
import java.lang.IllegalStateException

class NewestCommentsViewModel(
    private val indexRepository: IndexRepository,
    private val commentRepository: CommentRepository
) : PaginatedViewModel<Comment>() {
    // This is used to redirect the view to the story details of the comment.
    private val _storyURL = MutableLiveData<String?>()

    val storyURL: LiveData<String?> = _storyURL

    /**
     * Fetch the latest comments in a paginated fashion
     */
    override suspend fun fetchData(context: Context, page: Int): PaginatedList<Comment>? {
        _loading.value = true

        // Load the data
        var newestComments: IndexService.NewestCommentsResponse? = null
        val error = wrapAPIError(context) {
            newestComments = indexRepository.getNewestComments(page)
        }

        _loading.value = false

        // Check for any errors
        return if (error != null) {
            _error.value = error
            null
        } else {
            PaginatedList(
                newestComments!!.comments,
                newestComments!!.page,
                newestComments!!.hasPreviousPage,
                newestComments!!.hasNextPage
            )
        }
    }

    // Duplicated here from StoryDetailsViewModel, because that is a CollectionViewModel and we are
    // a PaginatedViewModel. I wonder whether there is a better way...

    /**
     * Either cast or retract a vote for the given comment.
     */
    fun voteOnComment(context: Context, shortURL: String) {
        val comments = _items.value ?: return
        _error.value = null

        viewModelScope.launch {
            _error.value = wrapAPIError(context, {
                when (it) {
                    404 -> {
                        Log.d(TAG, "The comment doesn't exist.")
                        context.getString(R.string.error_comment_not_found)
                    }
                    else -> null
                }
            }) {
                commentRepository.voteOnComment(shortURL)
                val commentIndex = comments.indexOfFirst { it.shortURL == shortURL }
                if (commentIndex == -1)
                    throw IllegalStateException("Couldn't find the comment that exists in the list?!")
                comments[commentIndex].toggleVote()

                // Notify the adapter
                collectionChanged(commentIndex, 1, OperationType.UPDATED)
            }
        }
    }

    companion object {
        private const val TAG = "NewestCommentsViewModel"
    }
}