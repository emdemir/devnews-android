package org.devnews.android.ui.story.create

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.base.Activity
import org.devnews.android.databinding.ActivityStoryCreateBinding
import org.devnews.android.repository.adapters.TagAdapter
import org.devnews.android.ui.story.details.StoryDetailsActivity.Companion.launchStoryDetails
import org.devnews.android.utils.TextChanged
import org.devnews.android.utils.dpToPx

class StoryCreateActivity : Activity() {
    private lateinit var binding: ActivityStoryCreateBinding
    private val viewModel: StoryCreateViewModel by viewModels {
        (application as DevNews).container.storyCreateViewModelFactory
    }

    // This will animate the removal of the tag chip.
    private val chipCloseClickListener = View.OnClickListener {
        val anim = AlphaAnimation(1f, 0f)
        anim.duration = 250

        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                viewModel.deselectTag(it.tag.toString())
                (it.parent as ChipGroup).removeView(it)

                // Validate tags
                binding.storyTagsSpinner.error = viewModel.validateTags(it.context)
            }

            override fun onAnimationStart(animation: Animation?) {}
        })

        it.startAnimation(anim)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup content view using binding
        binding = ActivityStoryCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel

        setupToolbar(true)

        // --- Validation Setup ---

        // Setup validation for the text.
        binding.storyTitleText.run {
            editText!!.addTextChangedListener(TextChanged {
                error = viewModel.validateTitle(this@StoryCreateActivity)
            })
        }
        binding.storyUrlText.run {
            editText!!.addTextChangedListener(TextChanged {
                error = viewModel.validateURL(this@StoryCreateActivity)
                    ?: viewModel.validateURLOrText(this@StoryCreateActivity)
            })
        }
        binding.storyTextText.run {
            editText!!.addTextChangedListener(TextChanged {
                error = viewModel.validateText(this@StoryCreateActivity)
                    ?: viewModel.validateURLOrText(this@StoryCreateActivity)
            })
        }

        // --- Tag List Setup ---

        // Setup adapter for tag list
        val adapter = TagAdapter(this)
        binding.storyTagsTextView.setAdapter(adapter)

        // When an operation happens, re-fill the adapter.
        viewModel.operation.observe(this) {
            if (it == null) return@observe

            adapter.addAll(viewModel.items.value!!)
        }

        // When an item is removed, remove it from the adapter.
        viewModel.removedItem.observe(this) {
            it?.run { adapter.remove(this) }
            binding.storyTagsTextView.setText("")
        }
        // When an item is added, add it to the adapter.
        viewModel.addedItem.observe(this) {
            it?.run { adapter.add(this) }
            // This isn't useless. For some reason this finally wakes up the AutoCompleteTextView
            // and makes it refresh the dropdown ListView.
            binding.storyTagsTextView.run { text = text }
        }

        // When a tag item is clicked, create a new chip and add it to the chip group.
        binding.storyTagsTextView.setOnItemClickListener { _, _, position, _ ->
            val tag = adapter.getItem(position)!!

            // Create the new chip, but only if this tag wasn't selected before.
            if (viewModel.selectTag(tag.name)) {
                val chip = Chip(this)
                chip.text = tag.name
                chip.tag = tag.name
                chip.isCloseIconVisible = true
                chip.shapeAppearanceModel = chip.shapeAppearanceModel.toBuilder().setAllCornerSizes(
                    dpToPx(this, 6f).toFloat()
                ).build()
                chip.isClickable = true
                chip.isFocusable = true
                chip.setOnClickListener(chipCloseClickListener)

                binding.storyTagsGroup.addView(chip)
            }

            // Validate for any errors
            binding.storyTagsSpinner.error = viewModel.validateTags(this)
        }

        // --- Submission Setup ---

        binding.createStoryFab.setOnClickListener {
            // Update all validation statuses
            binding.storyTitleText.error = viewModel.validateTitle(this)
            binding.storyUrlText.error =
                viewModel.validateURL(this) ?: viewModel.validateURLOrText(this)
            binding.storyTextText.error =
                viewModel.validateText(this) ?: viewModel.validateURLOrText(this)
            binding.storyTagsSpinner.error = viewModel.validateTags(this)

            // Try to submit the story
            viewModel.submitStory(this)
        }

        // If the story URL is set, then we are done here, so go to the story details.
        viewModel.storyURL.observe(this) {
            if (it == null) return@observe

            launchStoryDetails(this, it)
            finish()
        }

        viewModel.error.observe(this) {
            if (it == null) return@observe

            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }

        // --- Kicking it off ---

        // Load all tags.
        lifecycleScope.launchWhenStarted {
            viewModel.loadTags(this@StoryCreateActivity)
        }
    }

    companion object {

        /**
         * Launch the story creation activity.
         *
         * @param context Activity context
         */
        fun launchStoryCreate(context: Context) {
            context.startActivity(Intent(context, StoryCreateActivity::class.java))
        }
    }
}