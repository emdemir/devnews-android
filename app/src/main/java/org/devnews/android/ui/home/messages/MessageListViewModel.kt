package org.devnews.android.ui.home.messages

import android.content.Context
import org.devnews.android.repository.objects.Message
import org.devnews.android.base.PaginatedViewModel
import org.devnews.android.repository.MessageRepository
import org.devnews.android.repository.wrapAPIError

class MessageListViewModel(private val messageRepository: MessageRepository) :
    PaginatedViewModel<Message>() {

    /**
     * Fetch messages for this user on the given page.
     *
     * @param
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
}