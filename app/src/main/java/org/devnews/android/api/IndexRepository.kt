package org.devnews.android.api

import org.devnews.android.api.objects.Story

class IndexRepository(private val indexService: IndexService) {
    /**
     * Return the list of stories from the frontpage.
     */
    suspend fun getIndex(): List<Story> = indexService.getIndex().stories
}