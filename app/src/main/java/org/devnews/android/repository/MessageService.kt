package org.devnews.android.repository

import org.devnews.android.repository.objects.Message
import retrofit2.http.*

interface MessageService {
    @GET("/m/")
    suspend fun getMessages(@Query("page") page: Int = 1): MessageResponse

    @POST("/m/")
    suspend fun composeMessage(@Body message: MessageCreate): Message

    @GET("/m/{thread_id}/")
    suspend fun getThread(@Path("thread_id") threadID: Int): MessageResponse

    @POST("/m/{thread_id}/")
    suspend fun replyThread(@Path("thread_id") threadID: Int, @Body message: MessageReply): Message

    data class MessageCreate(val recipient: String, val message: String)
    data class MessageReply(val message: String)
    data class MessageResponse(val messages: List<Message>)
}