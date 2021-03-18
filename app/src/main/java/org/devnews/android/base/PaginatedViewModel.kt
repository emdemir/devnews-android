package org.devnews.android.base

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch

/**
 * A CollectionViewModel subclass that fetches data in pages.
 */
abstract class PaginatedViewModel<T> : CollectionViewModel<T>() {
    private val _page = MutableLiveData(0)
    private val _lastPage = MutableLiveData(false)

    val page: LiveData<Int> = _page
    val lastPage: LiveData<Boolean> = _lastPage

    /**
     * Fetch data from the server for the given page.
     * This function should always load the (N+1)th page.
     * Returns null if there was an error in fetching the data. You should set the _error value
     * directly, since there's no way to know whether there has been an error or not.
     *
     * @param context Android context
     * @param page The page to load data for
     */
    protected abstract suspend fun fetchData(context: Context, page: Int): PaginatedList<T>?

    /**
     * Reset all Observables to initial status.
     */
    override fun resetState() {
        super.resetState()
        _page.value = 0
        _lastPage.value = false
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
            val response = fetchData(context, page + 1) ?: return@launch
            response.items.forEach { items.add(it) }

            collectionChanged(prevSize, response.items.size, OperationType.ADDED)
            _page.value = response.page
            _lastPage.value = !response.hasNextPage
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


    /**
     * This is the type that fetchData must return.
     *
     * @param items The items returned for this page.
     * @param page The page number.
     * @param hasPreviousPage Whether this page has a page before it.
     * @param hasNextPage Whether this page has a page after it.
     */
    protected data class PaginatedList<T>(
        val items: List<T>,
        val page: Int,
        val hasPreviousPage: Boolean,
        val hasNextPage: Boolean
    )
}