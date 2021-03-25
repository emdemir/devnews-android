package org.devnews.android.base

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.devnews.android.R

/**
 * Dialog fragment that sets itself to sit at the bottom, right above the keyboard. This is useful
 * for showing forms that don't require a completely new activity.
 */
abstract class BottomDialogFragment : DialogFragment() {
    private var parent: ViewGroup? = null

    /**
     * Return the view the AlertDialog should contain.
     *
     * @param container The container ViewGroup
     * @param savedInstanceState The saved instance state.
     */
    abstract fun createView(container: ViewGroup?, savedInstanceState: Bundle?): View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null)
            throw IllegalStateException("Activity must not be null while creating the dialog!")

        // Instantiate the AlertDialog.
        val view = createView(parent, savedInstanceState)
        val builder = MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_DevNews_BottomDialog
        )
        builder.setView(view)

        // Put the dialog right above the user's keyboard, like in the YouTube app.
        val dialog = builder.create()
        val window = dialog.window!!
        val layoutParams = window.attributes
        layoutParams.gravity = Gravity.BOTTOM
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Make sure we store a reference to the container so we can use it to inflate later.
        parent = container
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}