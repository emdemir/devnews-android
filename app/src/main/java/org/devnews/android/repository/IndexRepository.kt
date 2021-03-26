package org.devnews.android.repository

class IndexRepository(private val indexService: IndexService) {
    /**
     * Return the list of stories from the frontpage.
     */
    suspend fun getIndex(page: Int = 1) = indexService.getIndex(page)

    /**
     * Return the most recently submitted stories.
     */
    suspend fun getRecentStories(page: Int = 1) = indexService.getRecentStories(page)

    /**
     * Return the newest comments on the site.
     */
    suspend fun getNewestComments(page: Int = 1) = indexService.getNewestComments(page)
}