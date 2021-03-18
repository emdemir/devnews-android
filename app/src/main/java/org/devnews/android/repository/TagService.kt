package org.devnews.android.repository

import com.google.gson.annotations.SerializedName
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
    ): StoriesWithTagResponse

    data class StoriesWithTagResponse(
        val tag: Tag,
        val stories: List<Story>,
        val page: Int,
        @SerializedName("has_prev_page") val hasPreviousPage: Boolean,
        @SerializedName("has_next_page") val hasNextPage: Boolean
    )
}