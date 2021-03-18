package org.devnews.android.ui.tag

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.repository.adapters.StoryAdapter
import org.devnews.android.base.Activity
import org.devnews.android.databinding.ActivityTagBinding
import org.devnews.android.ui.story.details.StoryDetailsActivity.Companion.launchStoryDetails
import org.devnews.android.utils.openCustomTab
import java.lang.IllegalStateException

class TagActivity : Activity() {
    private val viewModel: TagViewModel by viewModels {
        (application as DevNews).container.tagViewModelFactory
    }
    private lateinit var binding: ActivityTagBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fetch the tag from intent args.
        val tag = intent.getStringExtra(ARG_TAG)
            ?: throw IllegalStateException("TagActivity was not sent the tag name!")

        // --- View Setup ---

        // Setup view binding
        binding = ActivityTagBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Setup recycler and adapter
        val adapter = StoryAdapter(viewModel.items.value!!)
        binding.storyList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.storyList.adapter = adapter
        binding.storyList.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        // --- Tag Details Setup ---

        // When tag is loaded, show the tag details.
        viewModel.tag.observe(this) {
            if (it == null) {
                binding.tagDetails.visibility = GONE
            } else {
                binding.tagDetails.visibility = VISIBLE
                binding.tagChip.text = it.name
                binding.tagDescription.text = it.description
            }
        }

        binding.tagDetails.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            // Give SwipeRefreshLayout some top margin. We want the tag info to be displayed at the
            // top at all times however it would normally overlap on the top part of the layout,
            // so we can just juke it by giving SwipeRefresh the correct amount of margin. :^)
            //
            // This should be pretty safe, because tagDetails only re-layouts on 1) first load
            // 2) configuration change i.e. orientation, both cases in which we want to re-adjust
            // the margin anyway.
            val sr = binding.swipeRefresh
            val params = sr.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = binding.tagDetails.height
            sr.layoutParams = params
        }

        // --- Story List Setup ---

        // When an operation is performed on the collection send the adapter over to notify.
        viewModel.operation.observe(this) {
            if (it == null) return@observe
            viewModel.notifyAdapter(adapter)
        }

        // Setup the event handlers for each interaction
        adapter.setUpvoteClickListener {
            viewModel.voteOnStory(this, it)
        }
        adapter.setDetailsClickListener { url, storyType ->
            if (storyType == StoryAdapter.StoryType.URL) {
                openCustomTab(this, url)
            } else {
                launchStoryDetails(this, url)
            }
        }
        adapter.setCommentsClickListener {
            launchStoryDetails(this, it)
        }
        adapter.setTagClickListener {
            Log.d(TAG, "Clicked tag: $it")
            launchTagActivity(this, it)
        }

        // --- Load More Setup ---

        // If the adapter has scrolled past the last message, try to load more.
        adapter.setOnLoadMoreListener {
            viewModel.loadMore(this)
        }
        // If we have reached the last page, disable "load more".
        viewModel.lastPage.observe(this) {
            adapter.loadMore = !it
        }

        // --- Swipe to Refresh Setup ---

        binding.swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "Swipe to refresh onRefresh()")
            viewModel.loadFromStart(this)
        }
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.secondaryColor)
        binding.swipeRefresh.setColorSchemeResources(R.color.secondaryTextColor)

        // --- Loading/Progress Setup ---

        // Update the progress bar status.
        viewModel.loading.observe(this) {
            binding.progress.visibility = if (viewModel.items.value!!.isEmpty()) {
                if (it) VISIBLE else GONE
            } else {
                GONE
            }
            // Also let SwipeRefresh know we aren't refreshing anymore.
            if (!it) binding.swipeRefresh.isRefreshing = false
        }

        // --- Kicking it off ---

        // When activity is created, load stories.
        lifecycleScope.launchWhenCreated {
            viewModel.setTag(tag)
            viewModel.loadFromStart(this@TagActivity)
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
        const val ARG_TAG = "ARG_TAG"
        private const val TAG = "TagActivity"

        /**
         * Launch TagActivity with the selected tag.
         *
         * @param context Android context
         * @param tag The tag
         */
        fun launchTagActivity(context: Context, tag: String) {
            val intent = Intent(context, TagActivity::class.java)
            intent.putExtra(ARG_TAG, tag)
            context.startActivity(intent)
        }
    }
}