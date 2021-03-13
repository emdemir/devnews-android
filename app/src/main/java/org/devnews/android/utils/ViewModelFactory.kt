package org.devnews.android.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * This is a convenience wrapper to use the lambda signature of Kotlin in order to create ViewModel
 * factories.
 *
 * @param factory The factory function, producing the requested ViewModel
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory<V : ViewModel?>(
    private val factory: (modelClass: Class<V>) -> V
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return factory(modelClass as Class<V>) as T
    }
}