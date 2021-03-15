package org.devnews.android.api

import com.google.gson.annotations.SerializedName
import org.devnews.android.api.objects.Comment
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CommentService {
    @GET("/c/{short_url}/")
    suspend fun getComment(@Path("short_url") shortURL: String): Comment

    @POST("/c/")
    suspend fun createComment(@Body params: CommentCreate): Comment

    @POST("/c/{short_url}/vote")
    suspend fun voteOnComment(@Path("short_url") shortURL: String)

    data class CommentCreate(
        @SerializedName("story") val shortURL: String,
        val parent: String?, // Also short URL
        val comment: String
    )
}