package org.devnews.android.repository

class MessageRepository(private val messageService: MessageService) {
    /**
     * Get the current user's messages.
     *
     * @param page The page to fetch
     */
    suspend fun getMessages(page: Int = 1) = messageService.getMessages(page)

    /**
     * Compose a new message to a recipient.
     *
     * @param recipient The target user for the message.
     * @param message The contents of the message.
     */
    suspend fun composeMessage(recipient: String, message: String) =
        messageService.composeMessage(MessageService.MessageCreate(recipient, message))

    /**
     * Get the contents of a message thread.
     *
     * @param threadID The ID of the thread
     */
    suspend fun getThread(threadID: Int) = messageService.getThread(threadID)

    /**
     * Reply to a thread.
     *
     * @param threadID The ID of the thread.
     * @param message The contents of the message.
     */
    suspend fun replyThread(threadID: Int, message: String) =
        messageService.replyThread(threadID, MessageService.MessageReply(message))
}