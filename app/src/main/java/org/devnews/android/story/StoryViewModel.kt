package org.devnews.android.story

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.api.CommentRepository
import org.devnews.android.api.StoryRepository
import org.devnews.android.api.getError
import org.devnews.android.api.objects.Comment
import org.devnews.android.api.objects.Story
import org.devnews.android.api.wrapAPIError
import retrofit2.HttpException
import java.lang.IllegalStateException

class StoryViewModel(
    private val storyRepository: StoryRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {
    private val _story = MutableLiveData<Story?>()
    private val _comments = MutableLiveData<List<Comment>?>()
    private val _error = MutableLiveData<String?>()

    private val _parent = MutableLiveData<String?>()
    private val _updatedComment = MutableLiveData<String?>()

    private val _loading = MutableLiveData(false)
    private val _creatingComment = MutableLiveData(false)

    val story: LiveData<Story?> = _story
    val comments: LiveData<List<Comment>?> = _comments
    val loading: LiveData<Boolean> = _loading
    val creatingComment: LiveData<Boolean> = _creatingComment
    val error: LiveData<String?> = _error
    val updatedComment: LiveData<String?> = _updatedComment

    fun loadStory(context: Context, shortURL: String) {
        _story.value = null
        _error.value = null
        _loading.value = true

        viewModelScope.launch {
            _error.value = wrapAPIError(context, {
                when (it) {
                    404 -> {
                        Log.d(TAG, "The story doesn't exist.")
                        context.getString(R.string.error_story_not_found)
                    }
                    else -> null
                }
            }) {
                val story = storyRepository.getStoryByShortURL(shortURL)
                _story.value = story
                _comments.value = story.comments
            }

            _loading.value = false
        }
    }

    /**
     * Either cast or retract a vote on the story that's currently being displayed.
     *
     * @param context The activity context
     */
    fun voteOnStory(context: Context) {
        val story =
            _story.value ?: throw IllegalStateException("Voting on story but story is null?!")
        _error.value = null
        _loading.value = true

        viewModelScope.launch {
            _error.value = wrapAPIError(context, {
                when (it) {
                    404 -> context.getString(R.string.error_story_not_found)
                    else -> null
                }
            }) {
                storyRepository.voteOnStory(story.shortURL)
                val casting = story.userVoted != true
                val score = story.score

                story.score = score + if (casting) 1 else -1
                story.userVoted = casting
                // Knock the value to make the view re-render
                _story.value = story
            }

            _loading.value = false
        }
    }

    /**
     * Creates a new comment, and updates the list with the new comment.
     */
    fun createComment(context: Context, comment: String) {
        val story = _story.value ?: return
        val parent = _parent.value
        _error.value = null

        viewModelScope.launch {
            _creatingComment.value = true
            _updatedComment.value = null

            _error.value = wrapAPIError(context, {
                when (it) {
                    404 -> {
                        Log.d(TAG, "Either the parent comment or the story wasn't found.")
                        if (parent == null) {
                            context.getString(R.string.error_parent_comment_not_found)
                        } else {
                            context.getString(R.string.error_story_not_found)
                        }
                    }
                    else -> null
                }
            }) {
                val newComment = commentRepository.createComment(story.shortURL, comment, parent)

                // Update the list with the new comment.
                // Make sure to submit a new list so that RecyclerView knows to update.
                val newComments = ArrayList(_comments.value)
                if (parent != null) {
                    // Find the parent, and insert this comment under it
                    val parentComment = Comment.findComment(newComments) { it.shortURL == parent }
                    if (parentComment != null) {
                        if (parentComment.children == null)
                            parentComment.children = arrayListOf(newComment)
                        else
                            parentComment.children!!.add(newComment)
                    } else {
                        newComments.add(0, newComment)
                    }
                } else {
                    // Just insert the new comment as the first item
                    newComments.add(0, newComment)
                }
                _comments.value = newComments

                // Also increment the story's comment count and "knock" on it (setValue) to make
                // the view update it.
                story.commentCount++
                _story.value = story
            }

            _creatingComment.value = false
        }
    }

    /**
     * Prepare the ViewModel state for creating a comment.
     */
    fun setCommentingTarget(parent: String?) {
        _parent.value = parent
    }

    /**
     * Either cast or retract a vote for the given comment.
     */
    fun voteOnComment(context: Context, shortURL: String) {
        val comments = _comments.value ?: return
        _error.value = null

        viewModelScope.launch {
            _updatedComment.value = null

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
                // Find the comment, and toggle its voting status.
                val comment = Comment.findComment(comments) { it.shortURL == shortURL }
                if (comment != null) {
                    val casting = comment.userVoted != true
                    val score = comment.score!!

                    comment.userVoted = casting
                    comment.score = score + (if (casting) 1 else -1)
                }
                _updatedComment.value = shortURL
            }
        }
    }

    companion object {
        private const val TAG = "StoryViewModel"
    }
}