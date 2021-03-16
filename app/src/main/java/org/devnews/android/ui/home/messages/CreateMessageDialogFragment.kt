package org.devnews.android.ui.home.messages

import android.content.Context
import android.os.Bundle
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.motion.widget.TransitionBuilder.validate
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.base.BottomDialogFragment
import org.devnews.android.repository.MessageService
import org.devnews.android.ui.story.commenting.CreateCommentDialogFragment
import org.devnews.android.utils.TextChanged

class CreateMessageDialogFragment : BottomDialogFragment() {
    private val viewModel: MessageListViewModel by activityViewModels {
        (requireActivity().application as DevNews).container.messageListViewModelFactory
    }

    private lateinit var recipientText: TextInputLayout
    private lateinit var contentText: TextInputLayout
    private lateinit var sendButton: ImageView
    private lateinit var progress: ProgressBar

    override fun createView(container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = layoutInflater.inflate(R.layout.dialog_new_message, container, false)

        recipientText = view.findViewById(R.id.message_recipient)
        contentText = view.findViewById(R.id.message_content)
        sendButton = view.findViewById(R.id.send_button)
        progress = view.findViewById(R.id.progress)

        // Validate the contents of the text fields
        recipientText.editText!!.addTextChangedListener(TextChanged { validateRecipient() })
        contentText.editText!!.addTextChangedListener(TextChanged { validateContent() })
        // Request focus
        recipientText.requestFocus()

        // When the send button is pressed, validate the fields, and if both fields validate,
        // submit
        sendButton.setOnClickListener {
            if (this.validate()) {
                val recipient = recipientText.editText!!.text.toString()
                val content = contentText.editText!!.text.toString()
                viewModel.createMessage(requireContext(), recipient, content)
                setDialogEnabled(false)
            }
        }

        // Restore state if we had any.
        savedInstanceState?.run {
            recipientText.editText!!.setText(getString(KEY_RECIPIENT, ""))
            contentText.editText!!.setText(getString(KEY_CONTENT, ""))
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // If an error happened while this dialog is started, re-enable the controls
        viewModel.error.observe(this) {
            if (it == null) return@observe

            Log.d(TAG, "Got here!")
            setDialogEnabled(true)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_RECIPIENT, recipientText.editText!!.text.toString())
        outState.putString(KEY_CONTENT, contentText.editText!!.text.toString())
        super.onSaveInstanceState(outState)
    }

    private fun setDialogEnabled(enabled: Boolean) {
        isCancelable = enabled
        progress.visibility = if (enabled) GONE else VISIBLE
        sendButton.visibility = if (enabled) VISIBLE else INVISIBLE
        sendButton.isClickable = enabled
        recipientText.isEnabled = enabled
        contentText.isEnabled = enabled
    }

    private fun validate(): Boolean {
        var ret = true
        ret = validateRecipient() && ret
        ret = validateContent() && ret
        return ret
    }

    private fun validateRecipient(): Boolean {
        return when {
            TextUtils.isEmpty(recipientText.editText!!.text) -> {
                recipientText.error = getString(R.string.validate_recipient_empty)
                false
            }
            else -> {
                recipientText.error = null
                true
            }
        }
    }

    private fun validateContent(): Boolean {
        return when {
            TextUtils.isEmpty(contentText.editText!!.text) -> {
                contentText.error = getString(R.string.validate_content_empty)
                false
            }
            else -> {
                contentText.error = null
                true
            }
        }
    }

    companion object {
        const val TAG = "CreateMessageDialogFrag"
        const val KEY_RECIPIENT = "RECIPIENT"
        const val KEY_CONTENT = "CONTENT"
    }
}