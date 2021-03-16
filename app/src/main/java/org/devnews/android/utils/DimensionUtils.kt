package org.devnews.android.utils

import android.content.Context
import android.util.TypedValue

/**
 * Convert a dp value to pixels.
 */
fun dpToPx(context: Context, dp: Float): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}

/**
 * Convert an sp value to pixels.
 */
fun spToPx(context: Context, sp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp,
        context.resources.displayMetrics
    ).toInt()
}