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
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.devnews.android.R
import java.lang.ClassCastException
import java.lang.IllegalStateException

class CreateCommentDialogFragment : DialogFragment() {
    private var parent: ViewGroup? = null
    private lateinit var listener: CreateCommentDialogListener
    private lateinit var commentEditText: EditText
    private lateinit var sendButton: ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Instantiate the AlertDialog.
            val view = layoutInflater.inflate(R.layout.new_comment_box, parent, false)
            val builder = MaterialAlertDialogBuilder(
                requireContext(),
                R.style.ThemeOverlay_DevNews_CommentBox
            )
            builder.setView(view)

            // Put the dialog right above the user's keyboard, like in the YouTube app.
            val dialog = builder.create()
            val window = dialog.window!!
            val layoutParams = window.attributes
            layoutParams.gravity = BOTTOM
            window.setLayout(MATCH_PARENT, WRAP_CONTENT)
            window.attributes = layoutParams

            // Remove the padding enforced by DecorView. This involves resetting the drawable used by
            // Android to a simple ColorDrawable.
            val appBackgroundValue = TypedValue()
            requireContext().theme.resolveAttribute(
                android.R.attr.colorBackground,
                appBackgroundValue,
                true
            )
            val drawable = ColorDrawable(appBackgroundValue.data)
            window.setBackgroundDrawable(drawable)

            // For some reason, this is reset. Set it back to false.
            commentEditText = view.findViewById(R.id.new_comment_text)
            commentEditText.isSingleLine = false
            // Request focus.
            commentEditText.requestFocus()
            // If we had saved instance state, restore the message in the comment box.
            savedInstanceState?.run {
                commentEditText.setText(getString(KEY_MESSAGE))
            }

            // When the send button is pressed, activate submit.
            sendButton = view.findViewById(R.id.send_button)
            sendButton.setOnClickListener {
                listener.onSendComment(commentEditText.text.toString())
                dismiss()
            }

            dialog
        } ?: throw IllegalStateException("Activity can't be null!")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parent = container
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Make sure our host fragment/activity is listening.
        try {
            listener = context as CreateCommentDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement CreateCommentDialogListener!")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_MESSAGE, commentEditText.text.toString())
        super.onSaveInstanceState(outState)
    }

    interface CreateCommentDialogListener {
        fun onSendComment(message: String)
    }

    companion object {
        const val TAG = "CreateCommentDialogFragment"
        private const val KEY_MESSAGE = "CREATE_COMMENT_MESSAGE"
    }
}