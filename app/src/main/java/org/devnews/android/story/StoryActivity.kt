package org.devnews.android.story

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.Toolbar
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.api.adapters.CommentAdapter
import org.devnews.android.base.Activity
import org.devnews.android.databinding.ActivityStoryBinding
import org.devnews.android.api.adapters.StoryAdapter
import java.lang.IllegalStateException

class StoryActivity : Activity() {
    private val viewModel: StoryViewModel by viewModels(factoryProducer = {
        (application as DevNews).container.storyViewModelFactory
    })
    private lateinit var binding: ActivityStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup binding and content view
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Setup recycler for comments
        val adapter = CommentAdapter(listOf())
        binding.commentList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.commentList.adapter = adapter
        binding.commentList.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        // Get the story key
        val shortURL = intent.getStringExtra(KEY_SHORT_URL)
            ?: throw IllegalStateException("Short URL was not sent to story activity!")

        // Load the story
        lifecycleScope.launchWhenCreated {
            viewModel.loadStory(this@StoryActivity, shortURL)
        }

        // Bind the item holder
        val storyItem: View = findViewById(R.id.story_list_item)
        val viewHolder = StoryAdapter.ViewHolder(storyItem)
        // When the story is loaded, update the view holder with story data
        viewModel.story.observe(this) {
            if (it == null) return@observe
            viewHolder.bindData(it, enableStoryListener = false)

            it.comments?.let { comments ->
                adapter.submitList(comments)
            }
        }

        // When loading is complete, hide the progress bar and show the view
        viewModel.loading.observe(this) {
            binding.storyContainer.visibility = if (it) GONE else VISIBLE
            binding.progress.visibility = if (it) VISIBLE else GONE
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
        const val KEY_SHORT_URL = "SHORT_URL"
    }
}