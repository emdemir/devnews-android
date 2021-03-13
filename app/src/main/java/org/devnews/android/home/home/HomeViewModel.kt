package org.devnews.android.home.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.api.IndexRepository
import org.devnews.android.api.StoryService
import org.devnews.android.api.getError
import org.devnews.android.api.objects.Story
import org.devnews.android.welcome.LoginViewModel
import retrofit2.HttpException

class HomeViewModel(
    private val indexRepository: IndexRepository
) : ViewModel() {
    private val _loading = MutableLiveData(false)
    private val _stories = MutableLiveData<List<Story>?>()
    private val _error = MutableLiveData<String?>()

    val loading: LiveData<Boolean> = _loading
    val stories: LiveData<List<Story>?> = _stories
    val error: LiveData<String?> = _error

    fun loadStories(context: Context) {
        viewModelScope.launch {
            _error.value = null
            _loading.value = true

            try {
                _stories.value = indexRepository.getIndex()
            } catch (e: HttpException) {
                Log.e(TAG, "Homepage load failure with HTTP error code", e)

                when (e.code()) {
                    400 -> {
                        Log.d(TAG, "Bad request, apparently.")
                        _error.value = getError(e.response()!!)
                    }
                    else -> {
                        Log.d(TAG, "Unknown error")
                        _error.value = context.getString(R.string.error_unknown)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Homepage load failure!", e)
                _error.value = context.getString(R.string.error_unknown)
            } finally {
                _loading.value = false
            }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}