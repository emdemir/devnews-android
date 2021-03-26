package org.devnews.android.ui.home.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.databinding.FragmentNewestCommentsBinding
import org.devnews.android.repository.adapters.CommentAdapter
import org.devnews.android.ui.story.details.StoryDetailsActivity.Companion.launchStoryDetails
import org.devnews.android.utils.setErrorState
import org.devnews.android.utils.setProgressState
import org.devnews.android.utils.setupRecyclerView

class NewestCommentsFragment : Fragment() {
    private val viewModel: NewestCommentsViewModel by activityViewModels {
        (requireActivity().application as DevNews).container.newestCommentsViewModelFactory
    }
    private lateinit var binding: FragmentNewestCommentsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewestCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // --- Comment List Setup ---

        val adapter = CommentAdapter(viewModel.items.value!!)
        adapter.setShowReplyButtons(false)
        setupRecyclerView(binding.commentList, adapter, true)

        // Setup the event handlers for each interaction

        // When the upvote button is pressed, vote on the comment.
        adapter.setOnUpvoteListener {
            viewModel.voteOnComment(requireContext(), it)
        }
        // When the username is clicked, launch the user details.
        adapter.setOnUsernameClickListener {
            launchStoryDetails(requireContext(), it.storyURL!!)
        }

        // When the ViewModel notifies us that an operation was done, pass the adapter to the
        // ViewModel so that it can notify the adapter.
        viewModel.operation.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            viewModel.notifyAdapter(adapter)
        }

        // --- Load More Setup ---

        // When the adapter notifies us that the user has reached the end of the page, load more
        // items from the viewmodel.
        adapter.setOnLoadMoreListener {
            viewModel.loadMore(requireContext())
        }
        // If the ViewModel has reached the final page, disable load more until the whole front page
        // is refreshed.
        viewModel.lastPage.observe(viewLifecycleOwner) {
            adapter.loadMore = !it
        }

        // --- Swipe to Refresh Setup ---

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadFromStart(requireContext())
        }
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.secondaryColor)
        binding.swipeRefresh.setColorSchemeResources(R.color.secondaryTextColor)

        // --- Loading/Progress Setup ---

        // Show/hide progress bar based on the loading value, but only if we don't have any stories
        // yet.
        viewModel.loading.observe(viewLifecycleOwner) {
            setProgressState(
                it,
                viewModel.error.value,
                binding.progress,
                binding.commentList,
                binding.swipeRefresh
            )
        }

        // --- Error handling setup ---

        // If error message is reported to us, then show error with refresh button.
        viewModel.error.observe(viewLifecycleOwner) {
            if (viewModel.items.value!!.isNotEmpty() && it != null) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction(R.string.try_again) {
                        viewModel.loadMore(requireContext())
                    }.show()
            } else {
                setErrorState(it, binding.error)
            }
        }

        // Load stories when we are initially created
        lifecycleScope.launchWhenCreated {
            // Make sure that we don't already have stories first.
            if (viewModel.items.value!!.isEmpty()) {
                viewModel.loadFromStart(requireContext())
            }
        }
    }
}