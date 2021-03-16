package org.devnews.android.repository

import org.devnews.android.repository.objects.Story
import org.devnews.android.repository.objects.Tag
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TagService {
    @GET("/t/{tag}/")
    suspend fun getStoriesWithTag(
        @Path("tag") tag: String,
        @Query("page") page: Int = 1
    ): TagResponse

    data class TagResponse(
        val tag: Tag,
        val stories: List<Story>
    )
}