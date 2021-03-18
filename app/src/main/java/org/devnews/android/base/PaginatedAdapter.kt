package org.devnews.android.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import org.devnews.android.R

/**
 * A RecyclerView adapter which accepts a list of items, and also maintains an extra ViewHolder
 * which will fire a callback when visible. The view can then add more items to the list. This
 * allows the view to set up a "load more" pattern. The view can call .setLoadMore to control the
 * behavior (for instance, when the view has loaded the last page).
 *
 * NOTE: This adapter holds state for loading more items, and thus does not support multiple
 * RecyclerViews at this time. If the need to attach this adapter to multiple RecyclerViews ever
 * arises, that will need to be fixed somehow. Perhaps with a weak map of RecyclerView -> loadMore
 * state.
 *
 * It also only supports LinearLayoutManager.
 */
abstract class PaginatedAdapter<T, VH : RecyclerView.ViewHolder>(protected val items: List<T>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Whether to display the LoadMoreViewHolder.
    private var _loadMore = false

    // Listener for loading more items.
    private var onLoadMoreListener: (() -> Unit)? = null

    // Listener that fires when the view is scrolled. Will handle the load more callback.
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            (recyclerView.layoutManager as? LinearLayoutManager)?.run {
                val lastVisiblePosition = findLastCompletelyVisibleItemPosition()
                if (loadMore && lastVisiblePosition == items.size) {
                    onLoadMoreListener?.invoke()
                }
            }
        }
    }

    var loadMore
        get() = _loadMore
        set(value) {
            setLoadMoreInternal(value)
        }

    /**
     * Create the ViewHolder for your own list items.
     *
     * @param parent The parent ViewGroup
     */
    abstract fun createItemViewHolder(parent: ViewGroup, viewType: Int): VH

    /**
     * Bind your own item ViewHolder to a view.
     *
     * @param holder ViewHolder instance
     * @param position The item position
     */
    abstract fun bindItemViewHolder(holder: VH, position: Int)

    /**
     * Return an item ID for your items.
     *
     * @param position The item position
     */
    protected open fun onGetItemId(position: Int): Long {
        return NO_ID
    }

    /**
     * Return an view type for your items.
     *
     * @param position The item position
     */
    protected open fun onGetItemViewType(position: Int): Int {
        return 0
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(scrollListener)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        recyclerView.removeOnScrollListener(scrollListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_LOAD_MORE) {
            LoadMoreViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_load_more, parent, false)
            )
        } else {
            createItemViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (loadMore && position == items.size) {
            // Do nothing
        } else {
            // We will always get holders of type VH for items that aren't the last item in loadMore
            // mode.
            @Suppress("UNCHECKED_CAST")
            bindItemViewHolder(holder as VH, position)
        }
    }

    override fun getItemCount() = if (loadMore && items.isNotEmpty()) items.size + 1 else items.size

    override fun getItemId(position: Int): Long {
        return if (loadMore && position == items.size) {
            Long.MAX_VALUE // Low probability of being returned by items.
        } else {
            onGetItemId(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (loadMore && position == items.size) {
            TYPE_LOAD_MORE
        } else {
            onGetItemViewType(position)
        }
    }

    private fun setLoadMoreInternal(enabled: Boolean) {
        if (enabled == _loadMore) return
        _loadMore = enabled

        if (!enabled) {
            // The load more state has been disabled, remove our "Load More" ViewHolder
            notifyItemRemoved(items.size)
        } else {
            // The load more state has been enabled, notify the view that a new "item" has been
            // created (our "Load More" ViewHolder)
            notifyItemInserted(items.size - 1)
        }
    }

    /**
     * Set the listener that is fired when the progress bar is completely visible. The callback
     * should fire off the appropriate function for loading more items into the items list.
     *
     * @param listener The listener
     */
    fun setOnLoadMoreListener(listener: () -> Unit) {
        onLoadMoreListener = listener
    }

    /**
     * This class represents the view holder for the "Load More" item.
     */
    class LoadMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        // Number chosen to not collide with the subclass.
        private const val TYPE_LOAD_MORE = 32768
    }
}