package org.devnews.android.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel as BaseViewModel

/**
 * A ViewModel that contains things that every ViewModel needs, like "error" and "loading". This
 * way we can avoid repeating them.
 */
open class ViewModel : BaseViewModel() {
    protected val _loading = MutableLiveData(false)
    protected val _error = MutableLiveData<String?>()

    val loading: LiveData<Boolean> = _loading
    val error: LiveData<String?> = _error
}