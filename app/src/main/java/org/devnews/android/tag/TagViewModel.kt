package org.devnews.android.tag

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.api.TagRepository
import org.devnews.android.api.objects.Story
import org.devnews.android.api.objects.Tag
import org.devnews.android.api.wrapAPIError
import java.lang.IllegalStateException

class TagViewModel(private val tagRepository: TagRepository) : ViewModel() {
    private val _loading = MutableLiveData(false)
    private val _page = MutableLiveData(0)
    private val _loadedStoryCount = MutableLiveData<Int?>()
    private val _tagName = MutableLiveData<String?>()
    private val _tag = MutableLiveData<Tag?>()
    private val _stories = MutableLiveData<List<Story>>(ArrayList())
    private val _error = MutableLiveData<String?>()

    val loading: LiveData<Boolean> = _loading
    val page: LiveData<Int> = _page
    val loadedStoryCount: LiveData<Int?> = _loadedStoryCount
    val stories: LiveData<List<Story>> = _stories
    val error: LiveData<String?> = _error
    val tag: LiveData<Tag?> = _tag

    /**
     * Set the tag to fetch stories for to a new tag. Resets the page number, and clears the story
     * list.
     *
     * @param tag The new tag
     */
    fun setTag(tag: String) {
        _tagName.value = tag
        _tag.value = null
        (_stories.value!! as ArrayList<Story>).clear()
        _loadedStoryCount.value = null
        _page.value = 0
        _error.value = null
    }

    /**
     * Load the stories for the next page, and increment the page count.
     *
     * @param context The activity context
     * @param externalProgress If set, the loading value will not be changed. Indicates that something
     * like SwipeRefreshView is showing progress
     */
    fun loadStories(context: Context, externalProgress: Boolean = false) {
        val tag = _tagName.value
            ?: throw IllegalStateException("You must setTag() before loading stories!")
        val page = _page.value!!
        val stories = _stories.value!! as ArrayList<Story>

        if (!externalProgress)
            _loading.value = true

        viewModelScope.launch {
            _error.value = wrapAPIError(context, {
                when (it) {
                    404 -> {
                        Log.d(TAG, "Tag $tag not found.")
                        context.getString(R.string.error_tag_not_found)
                    }
                    else -> null
                }
            }) {
                val newStories = tagRepository.getStoriesWithTag(tag, page)
                newStories.stories.forEach { stories.add(it) }

                if (_tag.value == null) {
                    _tag.value = newStories.tag
                }

                _page.value = page + 1
                _loadedStoryCount.value = newStories.stories.size
            }

            if (!externalProgress)
                _loading.value = false
        }
    }

    companion object {
        private const val TAG = "TagViewModel"
    }
}