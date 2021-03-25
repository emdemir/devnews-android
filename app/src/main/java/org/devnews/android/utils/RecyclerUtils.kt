package org.devnews.android.utils

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Setup a recycler view's layout manager and attach an adapter.
 *
 * @param view The RecyclerView
 * @param adapter The adapter to attach
 * @param addDivider If true, a divider will be added between items
 */
fun setupRecyclerView(
    view: RecyclerView,
    adapter: RecyclerView.Adapter<*>,
    addDivider: Boolean = true
) {
    view.layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)
    view.adapter = adapter

    if (addDivider) {
        view.addItemDecoration(
            DividerItemDecoration(
                view.context,
                DividerItemDecoration.VERTICAL
            )
        )
    }
}