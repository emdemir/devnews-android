package org.devnews.android.api

import org.devnews.android.api.objects.Story

class IndexRepository(private val indexService: IndexService) {
    /**
     * Return the list of stories from the frontpage.
     */
    suspend fun getIndex(page: Int = 1): List<Story> = indexService.getIndex(page).stories
}