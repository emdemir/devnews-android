package org.devnews.android.ui.home.recent

import android.os.Bundle
import android.view.*
import android.view.View.GONE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.databinding.FragmentHomeBinding
import org.devnews.android.ui.adapters.StoryAdapter
import org.devnews.android.ui.story.details.StoryDetailsActivity
import org.devnews.android.ui.tag.TagActivity
import org.devnews.android.utils.openCustomTab
import org.devnews.android.utils.setErrorState
import org.devnews.android.utils.setProgressState
import org.devnews.android.utils.setupRecyclerView

class RecentFragment : Fragment() {
    private val viewModel: RecentViewModel by activityViewModels {
        (requireActivity().application as DevNews).container.recentViewModelFactory
    }
    // We can exploit the fact that the recents view and home view are identical, and just use the
// same fragment layout.
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // --- Story List Setup ---

        // Load the stories and adjust the RecyclerView
        val adapter = StoryAdapter(viewModel.items.value!!)
        setupRecyclerView(binding.storyList, adapter, true)

        // Setup the event handlers for each interaction
        adapter.setUpvoteClickListener {
            viewModel.voteOnStory(requireContext(), it)
        }
        adapter.setDetailsClickListener { url, storyType ->
            val context = requireContext()
            if (storyType == StoryAdapter.StoryType.URL) {
                openCustomTab(context, url)
            } else {
                StoryDetailsActivity.launchStoryDetails(context, url)
            }
        }
        adapter.setCommentsClickListener {
            StoryDetailsActivity.launchStoryDetails(requireContext(), it)
        }
        adapter.setTagClickListener {
            TagActivity.launchTagActivity(requireContext(), it)
        }

        // When the ViewModel notifies us that an operation was done, pass the adapter to the
        // ViewModel so that it can notify the adapter.
        viewModel.operation.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            viewModel.notifyAdapter(adapter)
        }

        // Hide the FAB because we don't use it here.
        binding.createStoryFab.visibility = GONE

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

        // --- Loading/Progress/Error Setup ---

        viewModel.loading.observe(viewLifecycleOwner) {
            setProgressState(
                it,
                viewModel.error.value,
                binding.progress,
                binding.storyList,
                binding.swipeRefresh
            )
        }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.swipe_refresh, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}