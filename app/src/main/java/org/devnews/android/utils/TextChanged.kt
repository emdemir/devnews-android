package org.devnews.android.utils

import android.text.Editable
import android.text.TextWatcher

/**
 * This class is a convenience wrapper for the {@link android.text.TextWatcher} interface.
 * It allows you to pass a single Kotlin lambda to {@link android.widget.EditText#addTextChangeListener()}
 * without having to add a bunch of empty functions.
 *
 * @param handler The handler function. Will be called with the text of the input.
 */
class TextChanged(private val handler: (s: CharSequence?) -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        handler(s)
    }
}
