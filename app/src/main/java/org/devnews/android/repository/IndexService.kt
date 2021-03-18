package org.devnews.android.repository

import com.google.gson.annotations.SerializedName
import org.devnews.android.repository.objects.Story
import retrofit2.http.GET
import retrofit2.http.Query

interface IndexService {
    @GET("/")
    suspend fun getIndex(@Query("page") page: Int): IndexResponse

    /**
     * Represents the response the index view returns.
     */
    data class IndexResponse(
        val stories: List<Story>,
        val page: Int,
        @SerializedName("has_prev_page") val hasPreviousPage: Boolean,
        @SerializedName("has_next_page") val hasNextPage: Boolean
    )
}