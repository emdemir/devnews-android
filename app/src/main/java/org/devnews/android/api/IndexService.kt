package org.devnews.android.api

import org.devnews.android.api.objects.Story
import retrofit2.http.GET

interface IndexService {
    @GET("/")
    suspend fun getIndex(): IndexResponse

    /**
     * Represents the response the index view returns.
     */
    data class IndexResponse(val stories: List<Story>)
}