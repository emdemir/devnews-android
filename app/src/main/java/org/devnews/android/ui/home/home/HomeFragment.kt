package org.devnews.android.ui.home.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.databinding.FragmentHomeBinding
import org.devnews.android.repository.adapters.StoryAdapter
import org.devnews.android.ui.story.details.StoryDetailsActivity.Companion.launchStoryDetails
import org.devnews.android.ui.tag.TagActivity.Companion.launchTagActivity
import org.devnews.android.utils.openCustomTab

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by activityViewModels {
        (requireActivity().application as DevNews).container.homeViewModelFactory
    }
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // --- Story List Setup ---

        // Load the stories and adjust the RecyclerView
        val adapter = StoryAdapter(viewModel.items.value!!)
        binding.storyList.adapter = adapter
        binding.storyList.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.storyList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        // Setup the event handlers for each interaction
        adapter.setUpvoteClickListener {
            viewModel.voteOnStory(requireContext(), it)
        }
        adapter.setDetailsClickListener { url, storyType ->
            val context = requireContext()
            if (storyType == StoryAdapter.StoryType.URL) {
                openCustomTab(context, url)
            } else {
                launchStoryDetails(context, url)
            }
        }
        adapter.setCommentsClickListener {
            launchStoryDetails(requireContext(), it)
        }
        adapter.setTagClickListener {
            Log.d(TAG, "Clicked tag: $it")
            launchTagActivity(requireContext(), it)
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
            Log.d(TAG, "Swipe to refresh onRefresh()")
            viewModel.loadFromStart(requireContext())
        }
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.secondaryColor)
        binding.swipeRefresh.setColorSchemeResources(R.color.secondaryTextColor)

        // --- Loading/Progress Setup ---

        // Show/hide progress bar based on the loading value, but only if we don't have any stories
        // yet.
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progress.visibility =
                if (viewModel.items.value!!.isEmpty() && !binding.swipeRefresh.isRefreshing) {
                    if (it) VISIBLE else GONE
                } else {
                    GONE
                }
            // Also let SwipeRefresh know we aren't refreshing anymore.
            if (!it) binding.swipeRefresh.isRefreshing = false
        }

        // --- Error handling setup ---

        // If error message is reported to us, then show error with refresh button.
        viewModel.error.observe(viewLifecycleOwner) {
            if (it != null) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction(R.string.try_again) {
                        viewModel.loadMore(requireContext())
                    }.show()
            }
        }

        // Load stories when we are initially created
        lifecycleScope.launchWhenCreated {
            // Make sure that we don't already have stories first.
            if (viewModel.items.value!!.isEmpty()) {
                viewModel.loadFromStart(requireContext())
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.swipe_refresh, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}