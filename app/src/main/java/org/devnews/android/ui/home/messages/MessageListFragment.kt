package org.devnews.android.ui.home.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.databinding.FragmentMessageListBinding
import org.devnews.android.repository.adapters.MessageAdapter
import org.devnews.android.ui.message.thread.MessageThreadActivity.Companion.launchMessageThread

class MessageListFragment : Fragment() {
    private val viewModel: MessageListViewModel by activityViewModels {
        (requireActivity().application as DevNews).container.messageListViewModelFactory
    }
    private lateinit var binding: FragmentMessageListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageListBinding.inflate(inflater, container, false)

        // --- Message List Setup ---

        // Setup the recycler view and its adapter
        val adapter = MessageAdapter(viewModel.items.value!!, true)
        binding.messageList.adapter = adapter
        binding.messageList.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.messageList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        // When a message is clicked, launch the message thread.
        adapter.setMessageClickListener {
            launchMessageThread(requireContext(), it)
        }

        // When an operation happens on the collection, notify the adapter.
        viewModel.operation.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            viewModel.notifyAdapter(adapter)
        }

        // --- Compose Message Setup ---

        // When the compose FAB is clicked, show the compose dialog.
        binding.createMessageFab.setOnClickListener {
            createMessageDialog()
        }
        // When the message ID is set by the view model (a new message thread was created),
        // launch that thread, possibly dismissing the dialog.
        viewModel.messageID.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            dismissDialogAndLaunchThread(it)
        }

        // --- Swipe to Refresh Setup ---

        // When swipe to refresh is pulled down reload the message list.
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadFromStart(requireContext())
        }
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.secondaryColor)
        binding.swipeRefresh.setColorSchemeResources(R.color.secondaryTextColor)

        // --- Loading/Progress Setup ---

        // Show/hide progress bar based on the loading value, but only if we don't have any stories
        // yet.
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progress.visibility =
                if (viewModel.items.value!!.isEmpty() && !binding.swipeRefresh.isRefreshing) {
                    if (it) View.VISIBLE else View.GONE
                } else {
                    View.GONE
                }
            // Also let SwipeRefresh know we aren't refreshing anymore.
            if (!it) binding.swipeRefresh.isRefreshing = false
        }

        // --- Error handling setup ---

        // If error message is reported to us, then show error with refresh button.
        viewModel.error.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            // If the dialog is displayed in front of us then display the snackbar on the dialog.
            val viewToShowIn = getDialogFragment()?.dialog?.window?.decorView ?: binding.root
            Snackbar.make(viewToShowIn, it, Snackbar.LENGTH_LONG).show()

            binding.swipeRefresh.isRefreshing = false
        }

        // Load stories when we are initially created
        lifecycleScope.launchWhenCreated {
            // Make sure that we don't already have stories first.
            if (viewModel.items.value!!.isEmpty()) {
                viewModel.loadFromStart(requireContext())
            }
        }

        return binding.root
    }

    /**
     * Creates a new "compose message" dialog.
     */
    private fun createMessageDialog() {
        CreateMessageDialogFragment().show(
            parentFragmentManager,
            CreateMessageDialogFragment.TAG
        )
    }

    /**
     * Get the "compose message" dialog if it exists.
     */
    private fun getDialogFragment(): DialogFragment? {
        val dialog = parentFragmentManager.findFragmentByTag(CreateMessageDialogFragment.TAG)

        return if (dialog != null) {
            (dialog as DialogFragment)
        } else {
            null
        }
    }

    /**
     * Dismiss the dialog, and launch MessageThreadActivity.
     */
    private fun dismissDialogAndLaunchThread(messageID: Int) {
        getDialogFragment()?.dismiss()
        launchMessageThread(requireContext(), messageID)
    }

    companion object {
        private const val TAG = "MessageListFragment"
    }
}