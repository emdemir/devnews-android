package org.devnews.android.story

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.api.StoryRepository
import org.devnews.android.api.getError
import org.devnews.android.api.objects.Story
import retrofit2.HttpException

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _story = MutableLiveData<Story?>()
    private val _loading = MutableLiveData(false)
    private val _error = MutableLiveData<String?>()

    val story: LiveData<Story?> = _story
    val loading: LiveData<Boolean> = _loading
    val error: LiveData<String?> = _error

    fun loadStory(context: Context, shortURL: String) {
        _story.value = null
        _loading.value = true

        viewModelScope.launch {
            try {
                _story.value = storyRepository.getStoryByShortURL(shortURL)
            } catch (e: HttpException) {
                Log.d(TAG, "Got an HTTP exception while loading the story.")

                when (e.code()) {
                    400 -> {
                        Log.d(TAG, "Bad request, apparently.")
                        _error.value = getError(e.response()!!)
                    }
                    404 -> {
                        Log.d(TAG, "The story doesn't exist.")
                        _error.value = context.getString(R.string.error_story_not_found)
                    }
                    else -> {
                        Log.d(TAG, "Some error we don't understand.")
                        _error.value = context.getString(R.string.error_unknown)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error while loading story!", e)
                _error.value = context.getString(R.string.error_unknown)
            } finally {
                _loading.value = false
            }
        }
    }

    companion object {
        private const val TAG = "StoryViewModel"
    }
}