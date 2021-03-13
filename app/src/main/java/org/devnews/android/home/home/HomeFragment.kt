package org.devnews.android.home.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.databinding.FragmentHomeBinding
import org.devnews.android.api.adapters.StoryAdapter

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by activityViewModels(factoryProducer = {
        (requireActivity().application as DevNews).container.homeViewModelFactory
    })
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val adapter = StoryAdapter(listOf())
        binding.storyList.adapter = adapter
        binding.storyList.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        // Display list after stories load
        viewModel.stories.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            adapter.submitList(it)
        }

        // Show/hide progress bar after animation completes
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progress.visibility = if (it) VISIBLE else GONE
        }

        // If error message is reported to us, then show error with refresh button.
        viewModel.error.observe(viewLifecycleOwner) {
            if (it != null) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction(R.string.refresh_button) {
                        viewModel.loadStories(requireContext())
                    }.show()
            }
        }

        // Load stories when we are initially created
        lifecycleScope.launchWhenCreated {
            // Make sure that we don't already have stories first.
            if (viewModel.stories.value == null) {
                viewModel.loadStories(requireContext())
            }
        }

        return binding.root
    }
}