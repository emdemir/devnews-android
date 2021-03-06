package org.devnews.android.ui.message.thread

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.base.Activity
import org.devnews.android.base.CollectionViewModel
import org.devnews.android.databinding.ActivityMessageThreadBinding
import org.devnews.android.ui.adapters.MessageThreadAdapter
import org.devnews.android.utils.setErrorState
import org.devnews.android.utils.setProgressState
import org.devnews.android.utils.setupRecyclerView

class MessageThreadActivity : Activity() {
    private val viewModel: MessageThreadViewModel by viewModels {
        (application as DevNews).container.messageThreadViewModelFactory
    }
    private lateinit var binding: ActivityMessageThreadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the thread ID key
        val threadID = intent.getIntExtra(ARG_THREAD_ID, 0)
        if (threadID == 0)
            throw IllegalStateException("Thread ID was not sent to message thread activity!")

        // --- View Setup ---

        // Setup binding and content view
        binding = ActivityMessageThreadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(true)

        // Setup recycler for the message thread
        val adapter = MessageThreadAdapter(viewModel.items.value!!)
        setupRecyclerView(binding.messageList, adapter)

        // --- Message List Setup ---

        // When an operation happens on the item list, notify the adapter.
        viewModel.operation.observe(this) {
            if (it == null) return@observe
            viewModel.notifyAdapter(adapter)

            if (it == CollectionViewModel.OperationType.ADDED) {
                // A new reply was added (or the view was refreshed, doesn't really matter), notify
                // the adapter (and the reply box ViewHolder in turn) about it.
                adapter.notifyReplyDone()
            }
        }

        // --- Reply Setup ---

        // When the user presses the send button on the reply box, reply to the thread.
        adapter.setReplyListener {
            viewModel.replyThread(this, it)
        }

        // --- Swipe Refresh Setup ---

        // When SwipeRefreshView is pulled, refresh the message thread.
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.setThreadID(threadID)
            viewModel.loadThread(this@MessageThreadActivity)
        }
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.secondaryColor)
        binding.swipeRefresh.setColorSchemeResources(R.color.secondaryTextColor)

        // --- Loading & Progress Bar Setup ---

        // Show progress bar while loading.
        viewModel.loading.observe(this) {
            if (viewModel.items.value!!.isEmpty()) {
                binding.messageList.visibility = if (it) GONE else VISIBLE
                binding.progress.visibility = if (it) VISIBLE else GONE
            } else {
                binding.messageList.visibility = VISIBLE
                binding.progress.visibility = GONE
            }

            // Also set isRefreshing = false if loading stopped.
            if (!it) binding.swipeRefresh.isRefreshing = false
        }

        viewModel.loading.observe(this) {
            setProgressState(
                it,
                viewModel.error.value,
                binding.progress,
                binding.messageList,
                binding.swipeRefresh
            )
        }

        // If an error happens, show a Snackbar.
        viewModel.error.observe(this) {
            if (viewModel.items.value!!.isNotEmpty() && it != null) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            } else {
                setErrorState(it, binding.error)
            }
        }

        // --- Kicking it Off ---

        // Load the message thread.
        lifecycleScope.launchWhenCreated {
            viewModel.setThreadID(threadID)
            viewModel.loadThread(this@MessageThreadActivity)
        }
    }

    companion object {
        const val ARG_THREAD_ID = "ARG_THREAD_ID"

        /**
         * Launch the message thread activity with the specified thread ID.
         *
         * @param context Android context
         * @param threadID The message thread ID
         */
        fun launchMessageThread(context: Context, threadID: Int) {
            val intent = Intent(context, MessageThreadActivity::class.java)
            intent.putExtra(ARG_THREAD_ID, threadID)
            context.startActivity(intent)
        }
    }
}