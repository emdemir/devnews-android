package org.devnews.android.ui.message.thread

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.repository.objects.Message
import org.devnews.android.base.CollectionViewModel
import org.devnews.android.repository.MessageRepository
import org.devnews.android.repository.wrapAPIError
import kotlin.IllegalStateException

class MessageThreadViewModel(private val messageRepository: MessageRepository) :
    CollectionViewModel<Message>() {
    private val _threadID = MutableLiveData<Int?>()

    /**
     * Set the message thread ID.
     */
    fun setThreadID(threadID: Int) {
        resetState()
        _threadID.value = threadID
    }

    /**
     * Load the message thread.
     *
     * @param context Android context
     */
    fun loadThread(context: Context) {
        _loading.value = true
        val threadID = _threadID.value ?: throw IllegalStateException("Thread ID was not set!")
        val items = _items.value as ArrayList<Message>

        viewModelScope.launch {
            _error.value = wrapAPIError(context, {
                when (it) {
                    404 -> context.getString(R.string.error_message_thread_not_found)
                    else -> null
                }
            }) {
                messageRepository.getThread(threadID).messages.forEach { items.add(it) }
                collectionChanged(0, items.size, OperationType.ADDED)
            }
            _loading.value = false
        }
    }

    /**
     * Reply to the current thread as the current user.
     *
     * @param context Android context
     * @param content The message contents
     */
    fun replyThread(context: Context, content: String) {
        _loading.value = true
        val threadID = _threadID.value ?: throw IllegalStateException("Thread ID was not set!")
        val items = _items.value!! as ArrayList<Message>

        viewModelScope.launch {
            _error.value = wrapAPIError(context, {
                when (it) {
                    // There are multiple 404 causes, show the message from the server.
                    404 -> context.getString(R.string.error_message_thread_not_found_reply)
                    else -> null
                }
            }) {
                val newMessage = messageRepository.replyThread(threadID, content)
                // Insert the message to the end.
                items.add(newMessage)
                // Notify the adapter.
                collectionChanged(items.size - 1, 1, OperationType.ADDED)
            }

            _loading.value = false
        }
    }
}