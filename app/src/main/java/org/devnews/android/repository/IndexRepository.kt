package org.devnews.android.repository

class IndexRepository(private val indexService: IndexService) {
    /**
     * Return the list of stories from the frontpage.
     */
    suspend fun getIndex(page: Int = 1) = indexService.getIndex(page)
}