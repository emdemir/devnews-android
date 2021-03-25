package org.devnews.android.ui.home.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.databinding.FragmentMessageListBinding
import org.devnews.android.repository.adapters.MessageListAdapter
import org.devnews.android.ui.message.thread.MessageThreadActivity.Companion.launchMessageThread
import org.devnews.android.utils.setErrorState
import org.devnews.android.utils.setProgressState
import org.devnews.android.utils.setupRecyclerView

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // --- Message List Setup ---

        // Setup the recycler view and its adapter
        val adapter = MessageListAdapter(viewModel.items.value!!)
        setupRecyclerView(binding.messageList, adapter, true)

        // When a message is clicked, launch the message thread.
        adapter.setMessageClickListener {
            launchMessageThread(requireContext(), it)
        }

        // When an operation happens on the collection, notify the adapter.
        viewModel.operation.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            viewModel.notifyAdapter(adapter)
        }

        // --- Load More Setup ---

        // If the adapter has scrolled past the last message, try to load more.
        adapter.setOnLoadMoreListener {
            viewModel.loadMore(requireContext())
        }
        // If we have reached the last page, disable "load more".
        viewModel.lastPage.observe(viewLifecycleOwner) {
            adapter.loadMore = !it
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
            // When we leave the messages segment of the app and come back, the ViewModel will
            // still be alive, however this fragment will be recreated. Because of this, the message ID
            // will cause the thread to automatically pop in. This prevents that.
            viewModel.resetMessageID()
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
            setProgressState(
                it,
                viewModel.error.value,
                binding.progress,
                binding.messageList,
                binding.swipeRefresh
            )
        }

        // --- Error handling setup ---

        // If error message is reported to us, then show error with refresh button.
        viewModel.error.observe(viewLifecycleOwner) {
            // If the dialog is displayed in front of us then display the snackbar on the dialog.
            val dialogView = getDialogFragment()?.dialog?.window?.decorView
            if (dialogView != null && it != null) {
                Snackbar.make(dialogView, it, Snackbar.LENGTH_LONG).show()
            } else if (viewModel.items.value!!.isNotEmpty() && it != null) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            } else {
                setErrorState(it, binding.error)
            }
        }

        // --- Kicking it off ---

        // Load stories when we are initially created
        lifecycleScope.launchWhenCreated {
            // Make sure that we don't already have stories first.
            if (viewModel.items.value!!.isEmpty()) {
                viewModel.loadFromStart(requireContext())
            }
        }

        // If we were asked to open the composer, then launch the CreateMessageDialog.
        val arguments = arguments
        if (arguments != null && arguments.getBoolean(ARG_COMPOSING, false)) {
            val username = arguments.getString(ARG_COMPOSE_TARGET)
                ?: throw IllegalStateException("Did not pass username with composing intent!")

            createMessageDialog(username)
        }

    }

    /**
     * Creates a new "compose message" dialog.
     *
     * @param username Recipient username to fill in automatically
     */
    private fun createMessageDialog(username: String? = null) {
        CreateMessageDialogFragment.newInstance(username).show(
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
        const val ARG_COMPOSING = "COMPOSING"
        const val ARG_COMPOSE_TARGET = "COMPOSE_TARGET"

    }
}