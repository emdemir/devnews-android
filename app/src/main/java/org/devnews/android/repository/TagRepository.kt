package org.devnews.android.repository

class TagRepository(private val tagService: TagService) {
    /**
     * Return a list of stories with the given tag.
     *
     * @param tag The tag to return stories for.
     * @param page If given, will return the given page number.
     */
    suspend fun getStoriesWithTag(tag: String, page: Int = 1) =
        tagService.getStoriesWithTag(tag, page)

    /**
     * Return a list of all tags on the site.
     */
    suspend fun getAllTags() = tagService.getAllTags()
}