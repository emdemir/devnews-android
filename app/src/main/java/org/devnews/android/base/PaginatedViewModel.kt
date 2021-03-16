package org.devnews.android.base

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * A CollectionViewModel subclass that fetches data in pages.
 */
abstract class PaginatedViewModel<T> : CollectionViewModel<T>() {
    protected val _page = MutableLiveData(0)

    val page: LiveData<Int> = _page

    /**
     * Fetch data from the server for the given page.
     * This function should always load the (N+1)th page.
     * Returns null if there was an error in fetching the data. You should set the _error value
     * directly, since there's no way to know whether there has been an error or not.
     *
     * @param context Android context
     * @param page The page to load data for
     */
    protected abstract suspend fun fetchData(context: Context, page: Int): List<T>?

    /**
     * Reset all Observables to initial status.
     */
    override fun resetState() {
        super.resetState()
        _page.value = 0
    }

    /**
     * Load a new page of data.
     *
     * @param context Android context
     */
    fun loadMore(context: Context) {
        val items = _items.value!! as ArrayList<T>
        val page = _page.value!!
        val prevSize = items.size

        viewModelScope.launch {
            val newItems = fetchData(context, page) ?: return@launch
            newItems.forEach { items.add(it) }

            collectionChanged(prevSize, newItems.size, OperationType.ADDED)
            _page.value = page + 1
        }
    }

    /**
     * Reset the page count and load the data from the beginning.
     *
     * @param context Android context
     */
    fun loadFromStart(context: Context) {
        resetState()
        loadMore(context)
    }
}