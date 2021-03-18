package org.devnews.android.ui.tag

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.devnews.android.R
import org.devnews.android.repository.StoryRepository
import org.devnews.android.repository.TagRepository
import org.devnews.android.repository.TagService
import org.devnews.android.repository.objects.Story
import org.devnews.android.repository.objects.Tag
import org.devnews.android.repository.wrapAPIError
import org.devnews.android.ui.common.StoryListViewModel
import java.lang.IllegalStateException

class TagViewModel(
    private val tagRepository: TagRepository,
    storyRepository: StoryRepository
) : StoryListViewModel(storyRepository) {
    private val _tagName = MutableLiveData<String?>()
    private val _tag = MutableLiveData<Tag?>()

    val tag: LiveData<Tag?> = _tag

    /**
     * Set the tag to fetch stories for to a new tag. Resets the page number, and clears the story
     * list.
     *
     * @param tag The new tag
     */
    fun setTag(tag: String) {
        resetState()
        _tag.value = null
        _tagName.value = tag
    }

    override suspend fun fetchData(context: Context, page: Int): PaginatedList<Story>? {
        val tag = _tagName.value
            ?: throw IllegalStateException("You must setTag() before loading stories!")

        _loading.value = true

        var response: TagService.StoriesWithTagResponse? = null
        val error = wrapAPIError(context, {
            when (it) {
                404 -> {
                    Log.d(TAG, "Tag $tag not found.")
                    context.getString(R.string.error_tag_not_found)
                }
                else -> null
            }
        }) {
            response = tagRepository.getStoriesWithTag(tag, page)

            if (_tag.value == null) {
                _tag.value = response!!.tag
            }
        }

        _loading.value = false

        return if (error == null) {
            PaginatedList(
                response!!.stories,
                response!!.page,
                response!!.hasPreviousPage,
                response!!.hasNextPage
            )
        } else {
            _error.value = error
            null
        }
    }

    companion object {
        private const val TAG = "TagViewModel"
    }
}