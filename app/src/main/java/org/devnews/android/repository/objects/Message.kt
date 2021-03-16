package org.devnews.android.repository.objects

import com.google.gson.annotations.SerializedName
import java.util.*

data class Message(
    val id: Int,
    val message: String,
    @SerializedName("message_html") val messageHtml: String,
    val author: String,
    val recipient: String,
    @SerializedName("sent_at") val sentAt: Date
)