package org.devnews.android.ui.story.details

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.base.Activity
import org.devnews.android.databinding.ActivityStoryDetailsBinding
import org.devnews.android.ui.adapters.CommentAdapter
import org.devnews.android.ui.adapters.StoryAdapter
import org.devnews.android.ui.story.details.commenting.CreateCommentDialogFragment
import org.devnews.android.ui.story.details.commenting.CreateCommentDialogFragment.Companion.CREATE_COMMENT_REQUEST
import org.devnews.android.ui.story.details.commenting.CreateCommentDialogFragment.Companion.KEY_COMMENT
import org.devnews.android.ui.user.UserDetailActivity.Companion.launchUserDetails
import org.devnews.android.utils.htmlToSpanned
import org.devnews.android.utils.openCustomTab
import org.devnews.android.utils.setErrorState
import org.devnews.android.utils.setProgressState

class StoryDetailsActivity : Activity() {
    private val viewModel: StoryDetailsViewModel by viewModels(factoryProducer = {
        (application as DevNews).container.storyViewModelFactory
    })
    private lateinit var binding: ActivityStoryDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the story key
        val shortURL = intent.getStringExtra(ARG_SHORT_URL)
            ?: throw IllegalStateException("Short URL was not sent to story activity!")

        // --- View Setup ---

        // Setup binding and content view
        binding = ActivityStoryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(true)

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
                binding.storyContents.setText(
                    htmlToSpanned(it.textHtml!!),
                    TextView.BufferType.SPANNABLE
                )
                binding.storyContents.visibility = VISIBLE
            }
        }

        viewHolder.setDetailsClickListener { url, storyType ->
            if (storyType == StoryAdapter.StoryType.URL) {
                openCustomTab(this, url)
            }
        }
        viewHolder.setUpvoteClickListener {
            viewModel.voteOnStory(this)
        }

        // --- "New Comment" box setup ---

        // Clicking the "new comment" box launches a dialog with the comment box.
        val newCommentText: EditText = findViewById(R.id.new_comment_text)
        val sendButton: ImageView = findViewById(R.id.send_button)
        // This is needed so we don't need to click the comment box twice to launch the dialog.
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
        // When the comment box sends a reply to our request for comments, submit the comment.
        supportFragmentManager.setFragmentResultListener(
            CREATE_COMMENT_REQUEST,
            this
        ) { _, bundle ->
            val comment = bundle.getString(KEY_COMMENT)!!
            viewModel.createComment(this, comment)
        }
        // When the upvote button is pressed, vote on the comment.
        adapter.setOnUpvoteListener {
            viewModel.voteOnComment(this, it)
        }
        // When the username is clicked, launch the user details.
        adapter.setOnUsernameClickListener {
            launchUserDetails(this, it.username!!)
        }

        viewModel.loading.observe(this) {
            setProgressState(
                it,
                viewModel.error.value,
                binding.progress,
                binding.storyContainer
            )
        }

        // Pop up a Snackbar if an error occurs, and if the story is null (something happened while
        // loading the story), display it in the middle of the screen.
        viewModel.error.observe(this) {
            if (viewModel.items.value!!.isNotEmpty() && it != null) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            } else {
                setErrorState(it, binding.error)
            }
        }

        // --- Kicking it off ---

        // Load the story
        lifecycleScope.launchWhenCreated {
            viewModel.loadStory(this@StoryDetailsActivity, shortURL)
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

    companion object {
        const val ARG_SHORT_URL = "SHORT_URL"

        /**
         * Launch story details for a given story short URL.
         *
         * @param context Android context
         * @param shortURL The short URL of the story
         */
        fun launchStoryDetails(context: Context, shortURL: String) {
            val intent = Intent(context, StoryDetailsActivity::class.java)
            intent.putExtra(ARG_SHORT_URL, shortURL)
            context.startActivity(intent)
        }
    }

}