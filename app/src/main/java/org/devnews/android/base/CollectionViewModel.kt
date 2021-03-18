package org.devnews.android.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalStateException

/**
 * A view model that contains a collection of items, and also LiveData properties for start and end
 * range modifications. It also provides a helper function to notify a RecyclerView adapter about
 * added/updated/removed data.
 */
open class CollectionViewModel<T> : ViewModel() {
    enum class OperationType { ADDED, UPDATED, REMOVED }

    protected val _items = MutableLiveData<List<T>>(ArrayList())
    private val _updateStart = MutableLiveData<Int?>()
    private val _updateCount = MutableLiveData<Int?>()
    private val _operation = MutableLiveData<OperationType?>()

    val items: LiveData<List<T>> = _items
    val operation: LiveData<OperationType?> = _operation

    /**
     * Reset all Observables to initial status.
     */
    protected open fun resetState() {
        val size = _items.value!!.size
        // Clear all data and errors
        (_items.value!! as ArrayList<T>).clear()
        _loading.value = false
        _error.value = null

        // We need to notify the Adapter about all the removed items, otherwise we'll get a crash
        // when we add new items.
        collectionChanged(0, size, OperationType.REMOVED)
    }

    /**
     * Set the parameters for a collection update notification.
     *
     * @param start The start index in the collection
     * @param count The number of items that were changed
     * @param operation The operation that took place
     */
    protected fun collectionChanged(start: Int, count: Int, operation: OperationType) {
        _updateStart.value = start
        _updateCount.value = count
        _operation.value = operation
    }

    /**
     * Send the correct notification to the adapter in order to update the view's contents correctly.
     *
     * @param adapter The adapter object
     */
    open fun <VH : RecyclerView.ViewHolder?> notifyAdapter(adapter: RecyclerView.Adapter<VH>) {
        val updateStart = _updateStart.value
            ?: throw IllegalStateException("updateStart must have a value before calling notifyAdapter")
        val updateCount = _updateCount.value
            ?: throw IllegalStateException("updateCount must have a value before calling notifyAdapter")
        val operation = _operation.value
            ?: throw IllegalStateException("operation must have a value before calling notifyAdapter")

        if (updateCount < 1)
            return

        when (operation) {
            OperationType.ADDED -> {
                if (updateCount > 1) {
                    adapter.notifyItemRangeInserted(updateStart, updateCount)
                } else {
                    adapter.notifyItemInserted(updateStart)
                }
            }
            OperationType.UPDATED -> {
                if (updateCount > 1) {
                    adapter.notifyItemRangeChanged(updateStart, updateCount)
                } else {
                    adapter.notifyItemChanged(updateStart)
                }
            }
            OperationType.REMOVED -> {
                if (updateCount > 1) {
                    adapter.notifyItemRangeRemoved(updateStart, updateCount)
                } else {
                    adapter.notifyItemRemoved(updateStart)
                }
            }
        }
    }
}