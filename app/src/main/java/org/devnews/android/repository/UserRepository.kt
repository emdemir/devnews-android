package org.devnews.android.repository

class UserRepository(private val userService: UserService) {
    /**
     * Get the details of a user.
     *
     * @param username The user's username.
     */
    suspend fun getUserDetails(username: String) = userService.getUserDetails(username)
}