package org.devnews.android.repository

import org.devnews.android.repository.objects.Story
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StoryService {
    @GET("/s/{short_url}/")
    suspend fun getStory(@Path("short_url") shortURL: String): Story

    @POST("/s/{short_url}/vote")
    suspend fun voteOnStory(@Path("short_url") shortURL: String)
}