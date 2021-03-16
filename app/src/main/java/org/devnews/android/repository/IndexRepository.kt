package org.devnews.android.repository

import org.devnews.android.repository.objects.Story

class IndexRepository(private val indexService: IndexService) {
    /**
     * Return the list of stories from the frontpage.
     */
    suspend fun getIndex(page: Int = 1): List<Story> = indexService.getIndex(page).stories
}