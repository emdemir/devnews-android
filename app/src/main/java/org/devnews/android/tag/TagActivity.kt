package org.devnews.android.tag

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
import org.devnews.android.api.adapters.StoryAdapter
import org.devnews.android.base.Activity
import org.devnews.android.databinding.ActivityTagBinding
import org.devnews.android.utils.dpToPx
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
        val adapter = StoryAdapter(viewModel.stories.value!!)
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

        // Setup the event handlers for each interaction
        adapter.setUpvoteClickListener {
            // viewModel.voteOnStory(this, it)
        }
        adapter.setDetailsClickListener { url, storyType ->
            if (storyType == StoryAdapter.StoryType.URL) {
                // openCustomTab(url)
            } else {
                // launchStory(url)
            }
        }
        adapter.setCommentsClickListener {
            // launchStory(it)
        }
        adapter.setTagClickListener {
            Log.d(TAG, "Clicked tag: $it")
        }

        // When loaded story count is updated, notify the adapter of the new items.
        viewModel.loadedStoryCount.observe(this) {
            if (it == null) return@observe

            Log.d(TAG, "Loaded new stories, count: $it")
            adapter.notifyItemRangeChanged(viewModel.stories.value!!.size - it, it)
            binding.swipeRefresh.isRefreshing = false
        }

        // --- Swipe to Refresh Setup ---

        binding.swipeRefresh.setOnRefreshListener {
            Log.d(TAG, "Swipe to refresh onRefresh()")

            // Take note of the current size of the story list
            val storyListSize = viewModel.stories.value!!.size
            // Reset the tag
            viewModel.setTag(tag)
            // Invalidate items in adapter
            adapter.notifyItemRangeRemoved(0, storyListSize)
            // Load the stories again
            viewModel.loadStories(this, externalProgress = true)
        }
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.secondaryColor)
        binding.swipeRefresh.setColorSchemeResources(R.color.secondaryTextColor)

        // --- Loading/Progress Setup ---

        // Update the progress bar status.
        // TODO: make this compatible with load more!
        viewModel.loading.observe(this) {
            binding.progress.visibility = if (it) VISIBLE else GONE
            binding.storyList.visibility = if (it) GONE else VISIBLE
        }

        // --- Kicking it off ---

        // When activity is created, load stories.
        lifecycleScope.launchWhenCreated {
            viewModel.setTag(tag)
            viewModel.loadStories(this@TagActivity)
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

        // The distance in dps from the bottom of the tag to the resting position of the
        // SwipeRefreshLayout progress spinner.
        const val SPINNER_DISTANCE = 8f
    }
}