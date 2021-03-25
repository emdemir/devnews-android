package org.devnews.android.repository

import com.google.gson.annotations.SerializedName
import org.devnews.android.repository.objects.Story
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StoryService {
    @GET("/s/{short_url}/")
    suspend fun getStory(@Path("short_url") shortURL: String): Story

    @POST("/s/{short_url}/vote")
    suspend fun voteOnStory(@Path("short_url") shortURL: String)

    @POST("/s/")
    suspend fun createStory(@Body storyData: StoryCreate): Story

    data class StoryCreate(
        val title: String,
        val url: String?,
        val text: String?,
        @SerializedName("is_authored") val isAuthored: Boolean,
        val tags: List<String>
    )
}