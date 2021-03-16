package org.devnews.android.ui.story

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.base.CollectionViewModel
import org.devnews.android.repository.CommentRepository
import org.devnews.android.repository.StoryRepository
import org.devnews.android.repository.objects.Comment
import org.devnews.android.repository.objects.Story
import org.devnews.android.repository.wrapAPIError
import java.lang.IllegalStateException

class StoryViewModel(
    private val storyRepository: StoryRepository,
    private val commentRepository: CommentRepository
) : CollectionViewModel<Comment>() {
    private val _story = MutableLiveData<Story?>()
    // Comments are presented as a tree from the API. We flatten them so that the view can display
    // them on a basic RecyclerView.
    private val _commentTree = MutableLiveData(ArrayList<Comment>())
    // The short URL of the parent comment to reply to. null for new root comment.
    private val _parent = MutableLiveData<String?>()

    val story: LiveData<Story?> = _story

    /**
     * Load the given story by its short URL.
     */
    fun loadStory(context: Context, shortURL: String) {
        _story.value = null
        _error.value = null
        _loading.value = true

        // Clear comments
        val items = _items.value!! as ArrayList<Comment>
        val oldSize = items.size
        items.clear()
        // Notify adapter about the comments
        collectionChanged(0, oldSize, OperationType.REMOVED)

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
                _commentTree.value = ArrayList(story.comments)

                // Generate the comment list
                Comment.generateCommentList(_commentTree.value!!).forEach { items.add(it) }
                collectionChanged(0, _items.value!!.size, OperationType.ADDED)
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
                story.toggleVote()
                // Knock the value to make it bind again.
                _story.value = story
            }

            _loading.value = false
        }
    }

    /**
     * Prepare the ViewModel state for creating a comment.
     */
    fun setCommentingTarget(parent: String?) {
        _parent.value = parent
    }

    /**
     * Creates a new comment, and updates the list with the new comment.
     */
    fun createComment(context: Context, comment: String) {
        val story = _story.value ?: return
        val parent = _parent.value
        _error.value = null
        val items = _items.value!! as ArrayList<Comment>

        viewModelScope.launch {
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
                // Create the comment
                val newComment = commentRepository.createComment(story.shortURL, comment, parent)
                // Update the list with the new comment. We exploit the fact that the children
                // array of each comment in the flattened comment list is untouched, so anything
                // we insert there will be automatically inserted in the comment tree too.
                val insertedIndex = if (parent == null) {
                    // If we were creating a new root comment then just insert it directly.
                    _commentTree.value!!.add(0, newComment)
                    items.add(0, newComment)
                    0
                } else {
                    // Find the index of the parent.
                    val parentIndex = items.indexOfFirst { it.shortURL == parent }
                    val parentComment = items[parentIndex]

                    // Get the children of the parent comment.
                    var children = parentComment.children
                    if (children == null) {
                        children = ArrayList()
                        parentComment.children = children
                    }

                    // Set the indent of the child
                    newComment.indent = parentComment.indent + 1

                    // Add the new comment as the first child.
                    children.add(0, newComment)
                    // Also insert to the flattened tree.
                    items.add(parentIndex + 1, newComment)
                    parentIndex + 1
                }

                // Notify the adapter.
                collectionChanged(insertedIndex, 1, OperationType.ADDED)

                // Also increment the story's comment count and "knock" on it (setValue) to make
                // the view update it.
                story.commentCount++
                _story.value = story
            }
        }
    }

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
        private const val TAG = "StoryViewModel"
    }
}