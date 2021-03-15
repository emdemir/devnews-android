package org.devnews.android.api

import org.devnews.android.api.objects.Story
import retrofit2.http.GET
import retrofit2.http.Query

interface IndexService {
    @GET("/")
    suspend fun getIndex(@Query("page") page: Int): IndexResponse

    /**
     * Represents the response the index view returns.
     */
    data class IndexResponse(val stories: List<Story>)
}