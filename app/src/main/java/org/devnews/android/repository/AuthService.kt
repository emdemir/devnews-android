package org.devnews.android.repository

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Exposes DevNews' authentication service.
 */
interface AuthService {
    @POST("/auth/token")
    suspend fun getToken(@Body params: TokenParams): Token
    @POST("/auth/renew")
    suspend fun renewToken(@Body token: Token): Token

    /**
     * Request data for token-related requests.
     */
    data class TokenParams(val username: String, val password: String)

    /**
     * Response data for token-related requests.
     */
    data class Token(val token: String)
}