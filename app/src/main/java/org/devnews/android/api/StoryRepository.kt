package org.devnews.android.api

class StoryRepository(private val storyService: StoryService) {
    /**
     * Return a short story from its short URL.
     *
     * @param shortURL - The short URL of the story
     */
    suspend fun getStoryByShortURL(shortURL: String) = storyService.getStory(shortURL)
}
