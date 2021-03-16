package org.devnews.android.repository

import android.util.Log

class AuthRepository(private val authService: AuthService) {
    /**
     * Returns a token for the given username and password.
     * Returns null if the user failed to authenticate.
     *
     * @param username The username of the user
     * @param password The password of the user
     */
    suspend fun getToken(username: String, password: String): String {
        Log.d(TAG, "getToken() username: $username")
        return authService.getToken(AuthService.TokenParams(username, password)).token
    }

    /**
     * Renew an existing token.
     * Returns null if the token couldn't be renewed.
     *
     * @param token The previous token
     */
    suspend fun renewToken(token: String): String {
        Log.d(TAG, "renewToken()")
        return authService.renewToken(AuthService.Token(token)).token
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}