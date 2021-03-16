package org.devnews.android.ui.story

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.TextUtils
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.repository.adapters.CommentAdapter
import org.devnews.android.base.Activity
import org.devnews.android.databinding.ActivityStoryBinding
import org.devnews.android.repository.adapters.StoryAdapter
import org.devnews.android.ui.story.commenting.CreateCommentDialogFragment
import java.lang.IllegalStateException

class StoryActivity : Activity(), CreateCommentDialogFragment.CreateCommentDialogListener {
    private val viewModel: StoryViewModel by viewModels(factoryProducer = {
        (application as DevNews).container.storyViewModelFactory
    })
    private lateinit var binding: ActivityStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the story key
        val shortURL = intent.getStringExtra(ARG_SHORT_URL)
            ?: throw IllegalStateException("Short URL was not sent to story activity!")

        // --- View Setup ---

        // Setup binding and content view
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Setup recycler for comments
        val adapter = CommentAdapter(viewModel.items.value!!)
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

            // If the story has text, format the text to a Spannable and display it.
            if (TextUtils.isEmpty(it.textHtml)) {
                binding.storyContents.visibility = GONE
            } else {
                val html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Html.fromHtml(it.textHtml, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    // We're already handling the deprecation case with the SDK version check.
                    @Suppress("DEPRECATION")
                    Html.fromHtml(it.textHtml)
                }
                val spannableHtml = SpannableString(html).trim()
                binding.storyContents.setText(spannableHtml, TextView.BufferType.SPANNABLE)
                binding.storyContents.visibility = VISIBLE
            }
        }

        viewHolder.setDetailsClickListener { _, storyType ->
            if (storyType == StoryAdapter.StoryType.URL) {
                viewModel.story.value!!.openCustomTab(this)
            }
        }
        viewHolder.setUpvoteClickListener {
            viewModel.voteOnStory(this)
        }

        // --- "New Comment" box setup ---

        // Clicking the "new comment" box launches a dialog with the comment box.
        val newCommentText: EditText = findViewById(R.id.new_comment_text)
        val sendButton: ImageView = findViewById(R.id.send_button)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            newCommentText.focusable = NOT_FOCUSABLE
        }
        newCommentText.setOnClickListener { createCommentBox() }
        sendButton.setOnClickListener { createCommentBox() }

        // --- Comments setup ---

        // When an operation is performed on the collection send the adapter over to notify.
        viewModel.operation.observe(this) {
            if (it == null) return@observe
            viewModel.notifyAdapter(adapter)
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

        // --- Progress bars & loading setup ---

        // When loading is complete, hide the progress bar and show the view
        viewModel.loading.observe(this) {
            if (viewModel.story.value == null) {
                binding.storyContainer.visibility = if (it) GONE else VISIBLE
                binding.progress.visibility = if (it) VISIBLE else GONE
            } else {
                binding.storyContainer.visibility = VISIBLE
                binding.progress.visibility = GONE
            }
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

        // --- Kicking it off ---

        // Load the story
        lifecycleScope.launchWhenCreated {
            viewModel.loadStory(this@StoryActivity, shortURL)
        }
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
        const val ARG_SHORT_URL = "SHORT_URL"

        /**
         * Launch story details for a given story short URL.
         *
         * @param context Android context
         * @param shortURL The short URL of the story
         */
        fun launchStoryDetails(context: Context, shortURL: String) {
            val intent = Intent(context, StoryActivity::class.java)
            intent.putExtra(ARG_SHORT_URL, shortURL)
            context.startActivity(intent)
        }
    }

}