package org.devnews.android.ui.home.recent

import android.content.Context
import org.devnews.android.repository.IndexRepository
import org.devnews.android.repository.IndexService
import org.devnews.android.repository.StoryRepository
import org.devnews.android.repository.objects.Story
import org.devnews.android.repository.wrapAPIError
import org.devnews.android.ui.common.StoryListViewModel

class RecentViewModel(
    private val indexRepository: IndexRepository,
    storyRepository: StoryRepository
) : StoryListViewModel(storyRepository) {
    /**
     * Fetch the stories for the current page.
     */
    override suspend fun fetchData(context: Context, page: Int): PaginatedList<Story>? {
        _loading.value = true

        // Load the data
        var newStories: IndexService.IndexResponse? = null
        val error = wrapAPIError(context) {
            newStories = indexRepository.getRecentStories(page)
        }

        _loading.value = false

        // Check for any errors
        return if (error != null) {
            _error.value = error
            null
        } else {
            PaginatedList(
                newStories!!.stories,
                newStories!!.page,
                newStories!!.hasPreviousPage,
                newStories!!.hasNextPage
            )
        }
    }
}