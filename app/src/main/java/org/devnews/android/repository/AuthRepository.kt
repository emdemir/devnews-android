package org.devnews.android.repository

import android.util.Log

class AuthRepository(private val authService: AuthService) {
    /**
     * Returns an access token for the given username and password.
     * Returns null if the user failed to authenticate.
     *
     * @param username The username of the user
     * @param password The password of the user
     * @param source The account source
     */
    suspend fun getAccessToken(
        username: String,
        password: String,
        source: String
    ): AuthService.AuthResponse {
        Log.d(TAG, "getAccessToken() username: $username")
        return authService.getAccessToken(
            AuthService.LoginParams(username, password),
            "openid",
            "code",
            source
        )
    }

    /**
     * Fetch a new access token by supplying a refresh token.
     *
     * @param token The refresh token
     */
    suspend fun refreshAccessToken(token: String): AuthService.AuthResponse {
        Log.d(TAG, "refreshAccessToken()")
        return authService.refreshAccessToken("Bearer $token")
    }

    /**
     * Fetch the OpenID identity token for this user via the access token.
     *
     * @param token The access token
     */
    suspend fun getIdentityToken(token: String): AuthService.AuthResponse {
        Log.d(TAG, "getIdentityToken()")
        return authService.getIdentityToken(AuthService.TokenParams(
            grantType = "authorization_code",
            code = token
        ))
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}