package org.devnews.android.story

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.View.NOT_FOCUSABLE
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.api.adapters.CommentAdapter
import org.devnews.android.base.Activity
import org.devnews.android.databinding.ActivityStoryBinding
import org.devnews.android.api.adapters.StoryAdapter
import org.devnews.android.api.objects.Comment
import org.devnews.android.story.commenting.CreateCommentDialogFragment
import java.lang.IllegalStateException

class StoryActivity : Activity(), CreateCommentDialogFragment.CreateCommentDialogListener {
    private val viewModel: StoryViewModel by viewModels(factoryProducer = {
        (application as DevNews).container.storyViewModelFactory
    })
    private lateinit var binding: ActivityStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- View Setup ---

        // Setup binding and content view
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Setup recycler for comments
        var flatCommentList: List<Comment> = ArrayList()
        val adapter = CommentAdapter(flatCommentList)
        binding.commentList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.commentList.adapter = adapter
        binding.commentList.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        // --- Story Setup ---

        // Bind the item holder
        val storyItem: View = findViewById(R.id.story_list_item)
        val viewHolder = StoryAdapter.ViewHolder(storyItem)
        // When the story is loaded, update the view holder with story data
        viewModel.story.observe(this) {
            if (it == null) return@observe
            viewHolder.bindData(it)
        }
        viewHolder.setDetailsClickListener { url, storyType ->
            if (storyType == StoryAdapter.StoryType.URL) {
                openCustomTab(url)
            }
        }
        viewHolder.setUpvoteClickListener {
            viewModel.voteOnStory(this)
        }

        // --- "New Comment" box setup ---

        // Clicking the "new comment" box launches a dialog with the comment box.
        val newCommentText: EditText = findViewById(R.id.new_comment_text)
        val sendButton: ImageView = findViewById(R.id.send_button)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            newCommentText.focusable = NOT_FOCUSABLE
        }
        newCommentText.setOnClickListener { createCommentBox() }
        sendButton.setOnClickListener { createCommentBox() }

        // --- Comments setup ---

        // Update the comments
        viewModel.comments.observe(this) {
            if (it == null) return@observe
            flatCommentList = Comment.generateCommentList(it)
            adapter.submitList(flatCommentList)
        }

        // When the reply button is clicked on one of the items in the adapter, also show the
        // comment box dialog.
        adapter.setOnReplyListener {
            createCommentBox(parent = it)
        }
        // When the upvote button is pressed, vote on the comment.
        adapter.setOnUpvoteListener {
            viewModel.voteOnComment(this, it)
        }

        // When a specific comment is updated, refresh only that comment in the adapter.
        viewModel.updatedComment.observe(this) { shortURL ->
            if (shortURL == null) return@observe

            // Find the comment
            val index = flatCommentList.indexOfFirst { it.shortURL == shortURL }
            if (index != -1)
                adapter.notifyItemChanged(index)
        }

        // --- Progress bars & loading setup ---

        // When loading is complete, hide the progress bar and show the view
        viewModel.loading.observe(this) {
            binding.storyContainer.visibility = if (it) GONE else VISIBLE
            binding.progress.visibility = if (it) VISIBLE else GONE
        }

        // When a comment is being created, disable click events.
        viewModel.creatingComment.observe(this) {
            // loading takes priority
            if (viewModel.loading.value != true)
                binding.progress.visibility = if (it) VISIBLE else GONE
            binding.root.isClickable = !it
        }

        // --- Error handling setup ---

        // Pop up a Snackbar if an error occurs, and if the story is null (something happened while
        // loading the story), display it in the middle of the screen.
        viewModel.error.observe(this) {
            if (it == null) return@observe

            // TODO: display it in the middle of the screen.
            if (viewModel.story.value != null) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            } else {
                // TODO
            }
        }

        // --- Loading the story ---

        // Get the story key
        val shortURL = intent.getStringExtra(KEY_SHORT_URL)
            ?: throw IllegalStateException("Short URL was not sent to story activity!")
        // Load the story
        lifecycleScope.launchWhenCreated {
            viewModel.loadStory(this@StoryActivity, shortURL)
        }
    }

    /**
     * Show the given URL with a browser Custom Tab.
     */
    private fun openCustomTab(url: String) {
        // TODO: can this be merged with the one in home.HomeFragment?
        val customTab = CustomTabsIntent.Builder().build()
        customTab.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTab.launchUrl(this, Uri.parse(url))
    }

    /**
     * Creates a new comment box with the correct parameters.
     */
    private fun createCommentBox(parent: String? = null) {
        // Set the current state for the comment reply.
        viewModel.setCommentingTarget(parent)
        CreateCommentDialogFragment().show(
            supportFragmentManager,
            CreateCommentDialogFragment.TAG
        )
    }

    /**
     * Called when the dialog receives a "send comment" action.
     */
    override fun onSendComment(message: String) {
        viewModel.createComment(this, message)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // If the user presses the <- button, finish activity
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val KEY_SHORT_URL = "SHORT_URL"
    }

}