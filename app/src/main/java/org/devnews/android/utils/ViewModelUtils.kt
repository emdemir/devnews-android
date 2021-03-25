package org.devnews.android.utils

import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * Update the state of the progress bar and the main content view based on the "loading" variable.
 * This function is intended to be used in a ViewModel observe callback.
 *
 * @param loading Whether the content is currently loading
 * @param error If non-null then everything will be hidden
 * @param progress The progress bar
 * @param content The main view showing the contents
 * @param swipeRefresh If given, will be updated/coordinated with the progress bar
 */
fun setProgressState(
    loading: Boolean,
    error: String?,
    progress: ProgressBar,
    content: View,
    swipeRefresh: SwipeRefreshLayout? = null
) {
    Log.d("setProgressState", "Called $loading $error ${swipeRefresh?.isRefreshing}")
    if (error != null) {
        progress.visibility = GONE
        content.visibility = GONE
    } else if (!loading || (swipeRefresh != null && swipeRefresh.isRefreshing)) {
        progress.visibility = GONE
        content.visibility = VISIBLE
    } else {
        progress.visibility = VISIBLE
        content.visibility = GONE
    }

    if (!loading && swipeRefresh != null)
        swipeRefresh.isRefreshing = false
}

/**
 * Set the state of the error text based on the error value. This function is intended to be used
 * in a ViewModel observe callback.
 *
 * @param
 */
fun setErrorState(error: String?, errorView: TextView) {
    if (error != null) {
        errorView.visibility = VISIBLE
        errorView.text = error
    } else {
        errorView.visibility = GONE
    }
}