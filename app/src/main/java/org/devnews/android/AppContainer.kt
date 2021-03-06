package org.devnews.android

import android.app.Activity
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import okhttp3.OkHttpClient
import org.devnews.android.api.*
import org.devnews.android.welcome.LoginViewModel
import org.devnews.android.welcome.RegisterViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class AppContainer(application: DevNews) {
    // Main retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.0.4:8081")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor(application))
            .build())
        .build()

    // Services
    private val authService = retrofit.create(AuthService::class.java)
    private val indexService = retrofit.create(IndexService::class.java)

    // Repositories
    val authRepository = AuthRepository(authService)
    val indexRepository = IndexRepository(indexService)

    // ViewModel factories
    val loginViewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LoginViewModel(authRepository) as T
        }
    }
    val registerViewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RegisterViewModel(authRepository) as T
        }
    }
}
