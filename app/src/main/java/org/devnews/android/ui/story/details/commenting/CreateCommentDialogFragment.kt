package org.devnews.android.ui.story.details.commenting

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import org.devnews.android.R
import org.devnews.android.base.BottomDialogFragment

class CreateCommentDialogFragment : BottomDialogFragment() {
    private lateinit var commentEditText: EditText
    private lateinit var sendButton: ImageView

    override fun createView(container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = layoutInflater.inflate(R.layout.dialog_new_comment, container, false)

        // For some reason, this is reset. Set it back to false.
        commentEditText = view.findViewById(R.id.new_comment_text)
        commentEditText.isSingleLine = false
        // Request focus.
        commentEditText.requestFocus()

        // When the send button is pressed, activate submit.
        sendButton = view.findViewById(R.id.send_button)
        sendButton.setOnClickListener {
            setFragmentResult(
                CREATE_COMMENT_REQUEST,
                bundleOf(KEY_COMMENT to commentEditText.text.toString())
            )
            dismiss()
        }

        // Restore state if we had any.
        savedInstanceState?.run {
            commentEditText.setText(getString(KEY_COMMENT, ""))
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_COMMENT, commentEditText.text.toString())
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val TAG = "CreateCommentDialogFragment"
        const val CREATE_COMMENT_REQUEST = "CREATE_COMMENT_RESULT"
        const val KEY_COMMENT = "COMMENT"
    }
}