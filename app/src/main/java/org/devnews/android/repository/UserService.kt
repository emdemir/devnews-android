package org.devnews.android.repository

import org.devnews.android.repository.objects.User
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("/u/{username}/")
    suspend fun getUserDetails(@Path("username") username: String): User
}