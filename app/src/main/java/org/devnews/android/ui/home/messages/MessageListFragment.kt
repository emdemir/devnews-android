package org.devnews.android.ui.home.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
    private val viewModel: MessageListViewModel by viewModels {
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
            if (it != null) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction(R.string.try_again) {
                        viewModel.loadMore(requireContext())
                    }.show()
            }
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
}