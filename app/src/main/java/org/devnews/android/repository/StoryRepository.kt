package org.devnews.android.repository

class StoryRepository(private val storyService: StoryService) {
    /**
     * Return a short story from its short URL.
     *
     * @param shortURL The short URL of the story
     */
    suspend fun getStoryByShortURL(shortURL: String) = storyService.getStory(shortURL)

    /**
     * Either cast or retract a vote on this story.
     *
     * @param shortURL The short URL of the story
     */
    suspend fun voteOnStory(shortURL: String) = storyService.voteOnStory(shortURL)
}
