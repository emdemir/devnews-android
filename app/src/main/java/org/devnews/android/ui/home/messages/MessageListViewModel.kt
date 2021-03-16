package org.devnews.android.ui.home.messages

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.repository.objects.Message
import org.devnews.android.base.PaginatedViewModel
import org.devnews.android.repository.MessageRepository
import org.devnews.android.repository.wrapAPIError

class MessageListViewModel(private val messageRepository: MessageRepository) :
    PaginatedViewModel<Message>() {
    // The created message thread ID. When this is set the Fragment will launch the message thread
    // activity.
    private val _messageID = MutableLiveData<Int?>()

    val messageID: LiveData<Int?> = _messageID

    /**
     * Fetch messages for this user on the given page.
     *
     * @param context Android content
     * @param page Page number to fetch
     */
    override suspend fun fetchData(context: Context, page: Int): List<Message>? {
        _loading.value = true

        var newMessages: List<Message>? = null
        val error = wrapAPIError(context) {
            newMessages = messageRepository.getMessages(page).messages
        }

        _loading.value = false

        return if (error == null) {
            newMessages
        } else {
            _error.value = error
            null
        }
    }

    /**
     * Create a new message thread.
     *
     * @param context Android context
     * @param recipient The recipient's username
     * @param content The message contents
     */
    fun createMessage(context: Context, recipient: String, content: String) {
        _loading.value = true

        viewModelScope.launch {
            _error.value = wrapAPIError(context, {
                when (it) {
                    404 -> context.getString(R.string.error_recipient_not_found)
                    else -> null
                }
            }) {
                val message = messageRepository.composeMessage(recipient, content)
                _messageID.value = message.id
            }

            _loading.value = false
        }
    }
}