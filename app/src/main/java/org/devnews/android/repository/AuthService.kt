package org.devnews.android.repository

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Exposes DevNews' authentication service.
 */
interface AuthService {
    @POST("/auth/login")
    suspend fun getAccessToken(
        @Body params: LoginParams,
        @Query("scope") scope: String,
        @Query("response_type") responseType: String,
        @Query("source") source: String
    ): AuthResponse

    @POST("/auth/refresh")
    suspend fun refreshAccessToken(@Header("Authorization") refreshToken: String): AuthResponse

    @POST("/auth/token")
    suspend fun getIdentityToken(@Body params: TokenParams): AuthResponse

    @POST("/auth/register")
    suspend fun registerUser(@Body params: RegisterParams): AuthResponse

    /**
     * Request data for fetching the access token.
     */
    data class LoginParams(val username: String, val password: String)

    /**
     * Request data for fetching the identity token.
     */
    data class TokenParams(@SerializedName("grant_type") val grantType: String, val code: String)

    /**
     * Request data for registering a new DevNews user.
     */
    data class RegisterParams(val username: String, val password: String, val email: String)

    /**
     * Response data for token-related requests.
     */
    data class AuthResponse(
        @SerializedName("id_token") val identityToken: String?,
        @SerializedName("access_token") val accessToken: String?,
        @SerializedName("refresh_token") val refreshToken: String?,
        @SerializedName("token_type") val tokenType: String?,
        @SerializedName("expires_in") val expiresIn: Int?
    )
}