package org.devnews.android.repository

import com.google.gson.annotations.SerializedName
import org.devnews.android.repository.objects.Message
import retrofit2.http.*

interface MessageService {
    @GET("/m/")
    suspend fun getMessages(@Query("page") page: Int = 1): MessageListResponse

    @POST("/m/")
    suspend fun composeMessage(@Body message: MessageCreate): Message

    @GET("/m/{thread_id}/")
    suspend fun getThread(@Path("thread_id") threadID: Int): MessageThreadResponse

    @POST("/m/{thread_id}/")
    suspend fun replyThread(@Path("thread_id") threadID: Int, @Body message: MessageReply): Message

    data class MessageCreate(val recipient: String, val message: String)
    data class MessageReply(val message: String)
    data class MessageListResponse(
        val messages: List<Message>,
        val page: Int,
        @SerializedName("has_prev_page") val hasPreviousPage: Boolean,
        @SerializedName("has_next_page") val hasNextPage: Boolean
    )
    data class MessageThreadResponse(val messages: List<Message>)
}