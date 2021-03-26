package org.devnews.android.repository

import com.google.gson.annotations.SerializedName
import org.devnews.android.repository.objects.Comment
import org.devnews.android.repository.objects.Story
import retrofit2.http.GET
import retrofit2.http.Query

interface IndexService {
    @GET("/")
    suspend fun getIndex(@Query("page") page: Int): IndexResponse

    @GET("/recent")
    suspend fun getRecentStories(@Query("page") page: Int): IndexResponse

    @GET("/comments")
    suspend fun getNewestComments(@Query("page") page: Int): NewestCommentsResponse

    /**
     * Represents the response the index and recent stories views return.
     */
    data class IndexResponse(
        val stories: List<Story>,
        val page: Int,
        @SerializedName("has_prev_page") val hasPreviousPage: Boolean,
        @SerializedName("has_next_page") val hasNextPage: Boolean
    )

    /**
     * Represents the response the newest comments view returns.
     */
    data class NewestCommentsResponse(
        val comments: List<Comment>,
        val page: Int,
        @SerializedName("has_prev_page") val hasPreviousPage: Boolean,
        @SerializedName("has_next_page") val hasNextPage: Boolean
    )
}