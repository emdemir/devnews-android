package org.devnews.android.api

class CommentRepository(private val commentService: CommentService) {
    /**
     * Get a comment by its short URL.
     *
     * @param shortURL The short URL for the comment.
     */
    suspend fun getCommentByShortURL(shortURL: String) = commentService.getComment(shortURL)

    /**
     * Create a new comment.
     *
     * @param storyURL The short URL for the story.
     * @param comment The contents of the comment in Markdown.
     * @param parent If given, will be created as a reply.
     */
    suspend fun createComment(storyURL: String, comment: String, parent: String? = null) =
        commentService.createComment(
            CommentService.CommentCreate(
                shortURL = storyURL,
                comment = comment,
                parent = parent
            )
        )

    suspend fun voteOnComment(shortURL: String) = commentService.voteOnComment(shortURL)
}