package org.devnews.android.ui.story.commenting

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity.BOTTOM
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.ImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.devnews.android.R
import org.devnews.android.base.BottomDialogFragment
import java.lang.ClassCastException
import java.lang.IllegalStateException

class CreateCommentDialogFragment : BottomDialogFragment<String>() {
    private lateinit var commentEditText: EditText
    private lateinit var sendButton: ImageView

    override fun createView(container: ViewGroup?): View {
        val view = layoutInflater.inflate(R.layout.dialog_new_comment, container, false)

        // For some reason, this is reset. Set it back to false.
        commentEditText = view.findViewById(R.id.new_comment_text)
        commentEditText.isSingleLine = false
        // Request focus.
        commentEditText.requestFocus()

        // When the send button is pressed, activate submit.
        sendButton = view.findViewById(R.id.send_button)
        sendButton.setOnClickListener {
            listener.onSubmit(commentEditText.text.toString())
            dismiss()
        }

        return view
    }

    companion object {
        const val TAG = "CreateCommentDialogFragment"
        private const val KEY_MESSAGE = "CREATE_COMMENT_MESSAGE"
    }
}