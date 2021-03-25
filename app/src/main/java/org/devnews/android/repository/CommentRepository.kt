package org.devnews.android.repository

class CommentRepository(private val commentService: CommentService) {

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