package org.devnews.android.ui.message.thread

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast.LENGTH_LONG
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.base.Activity
import org.devnews.android.databinding.ActivityMessageThreadBinding
import org.devnews.android.databinding.ActivityStoryBinding
import org.devnews.android.repository.adapters.CommentAdapter
import org.devnews.android.repository.adapters.MessageAdapter
import org.devnews.android.ui.story.StoryActivity
import java.lang.IllegalStateException

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

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Setup recycler for the message thread
        val adapter = MessageAdapter(viewModel.items.value!!, false)
        binding.messageList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.messageList.adapter = adapter
        binding.messageList.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        // --- Message List Setup ---

        // When an operation happens on the item list, notify the adapter.
        viewModel.operation.observe(this) {
            if (it == null) return@observe
            viewModel.notifyAdapter(adapter)
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
            binding.messageList.visibility = if (it) GONE else VISIBLE
            binding.progress.visibility = if (it) VISIBLE else GONE

            // Also set isRefreshing = false if loading stopped.
            if (!it) binding.swipeRefresh.isRefreshing = false
        }

        // --- Error Setup ---

        // If an error happens, show a Snackbar.
        viewModel.error.observe(this) {
            if (it == null) return@observe

            // TODO: show error in the middle of the screen if message thread is empty.
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }

        // --- Kicking it Off ---

        // Load the message thread.
        lifecycleScope.launchWhenCreated {
            viewModel.setThreadID(threadID)
            viewModel.loadThread(this@MessageThreadActivity)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // If the user presses the <- button, finish activity
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
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