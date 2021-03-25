package org.devnews.android.ui.story.create

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.base.CollectionViewModel
import org.devnews.android.repository.StoryRepository
import org.devnews.android.repository.StoryService
import org.devnews.android.repository.TagRepository
import org.devnews.android.repository.objects.Tag
import org.devnews.android.repository.wrapAPIError

class StoryCreateViewModel(
    private val tagRepository: TagRepository,
    private val storyRepository: StoryRepository
) : CollectionViewModel<Tag>() {
    // Two-way data binding with the view
    val title = MutableLiveData("")
    val url = MutableLiveData("")
    val text = MutableLiveData("")
    val isAuthored = MutableLiveData(false)

    // The below two are used to update the adapter.
    private val _removedItem = MutableLiveData<Tag?>()
    private val _addedItem = MutableLiveData<Tag?>()
    private val _selectedTags = MutableLiveData<Set<String>>(HashSet())
    private val _storyURL = MutableLiveData<String?>()

    // "items" in this ViewModel is a filtered list of tags based on what already isn't selected.
    // This list contains the full set of tags.
    private val _allTags = MutableLiveData<ArrayList<Tag>>(ArrayList())

    val removedItem: LiveData<Tag?> = _removedItem
    val addedItem: LiveData<Tag?> = _addedItem
    val storyURL: LiveData<String?> = _storyURL

    /**
     * Validate each field's value.
     *
     * @param context Android context
     */
    private fun validate(context: Context): Boolean {
        var res = true
        res = validateTitle(context) == null && res
        res = validateURL(context) == null && res
        res = validateText(context) == null && res
        res = validateURLOrText(context) == null && res
        res = validateTags(context) == null && res
        return res
    }

    fun validateTitle(context: Context): String? {
        return when {
            TextUtils.isEmpty(title.value) -> context.getString(R.string.story_create_title_empty)
            else -> null
        }
    }

    fun validateURL(context: Context): String? {
        val url = url.value ?: ""
        return when {
            url.contains(' ') -> context.getString(R.string.story_create_url_spaces)
            else -> null
        }
    }

    fun validateText(context: Context): String? {
        // any validation that isn't covered by the maximum length counter?
        return null
    }

    fun validateURLOrText(context: Context): String? {
        return when {
            TextUtils.isEmpty(url.value) && TextUtils.isEmpty(text.value) ->
                context.getString(R.string.story_create_no_url_or_text)
            !TextUtils.isEmpty(url.value) && !TextUtils.isEmpty(text.value) ->
                context.getString(R.string.story_create_both_url_and_text)
            else -> null
        }
    }

    fun validateTags(context: Context): String? {
        return when {
            _selectedTags.value!!.isEmpty() -> context.getString(R.string.story_create_tags_minimum)
            _selectedTags.value!!.size > 3 -> context.getString(R.string.story_create_tags_maximum)
            else -> null
        }
    }

    fun loadTags(context: Context) {
        resetState()
        _allTags.value = ArrayList()
        _loading.value = true
        val items = _items.value as ArrayList<Tag>

        viewModelScope.launch {
            _error.value = wrapAPIError(context) {
                val tags = tagRepository.getAllTags().tags
                tags.forEach { items.add(it) }
                _allTags.value = ArrayList(tags)

                collectionChanged(0, tags.size, OperationType.ADDED)
            }

            _loading.value = false
        }
    }

    /**
     * Submit the story with the given parameters if validation passes.
     *
     * @param context Android context
     */
    fun submitStory(context: Context) {
        if (!validate(context)) return
        _loading.value = true

        val title = title.value!!
        val url = url.value!!
        val text = text.value!!
        val isAuthored = isAuthored.value!!
        val selectedTags = _selectedTags.value!!

        viewModelScope.launch {
            _error.value = wrapAPIError(context) {
                val story = storyRepository.createStory(
                    StoryService.StoryCreate(
                        title,
                        if (TextUtils.isEmpty(url)) null else url,
                        if (TextUtils.isEmpty(text)) null else text,
                        isAuthored,
                        ArrayList(selectedTags)
                    )
                )
                _storyURL.value = story.shortURL
            }

            _loading.value = false
        }
    }

    /**
     * Select a tag.
     *
     * @param tagName name of the tag
     */
    fun selectTag(tagName: String): Boolean {
        val added = (_selectedTags.value as MutableSet<String>).add(tagName)
        if (added) {
            val tag = _allTags.value!!.find { it.name == tagName }!!
            _removedItem.value = tag
        }

        Log.d("StoryCreateViewModel", "Selected $tagName: $added")
        return added
    }


    /**
     * Deselect a tag.
     *
     * @param tagName name of the tag
     */
    fun deselectTag(tagName: String) {
        val removed = (_selectedTags.value as MutableSet<String>).remove(tagName)
        if (removed) {
            val tag = _allTags.value!!.find { it.name == tagName }!!
            _addedItem.value = tag
        }

        Log.d("StoryCreateViewModel", "Deselected $tagName: $removed")
    }
}