package org.devnews.android.repository

import com.google.gson.annotations.SerializedName
import org.devnews.android.repository.objects.Comment
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CommentService {

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