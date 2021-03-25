package org.devnews.android.utils

import android.content.Context

/**
 * Convert a dp value to pixels.
 */
fun dpToPx(context: Context, dp: Float): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}

