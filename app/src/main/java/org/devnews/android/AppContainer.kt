package org.devnews.android

import okhttp3.OkHttpClient
import org.devnews.android.api.*
import org.devnews.android.home.home.HomeViewModel
import org.devnews.android.story.StoryViewModel
import org.devnews.android.utils.ViewModelFactory
import org.devnews.android.welcome.LoginViewModel
import org.devnews.android.welcome.RegisterViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    private val storyService = retrofit.create(StoryService::class.java)

    // Repositories
    val authRepository = AuthRepository(authService)
    val indexRepository = IndexRepository(indexService)
    val storyRepository = StoryRepository(storyService)

    // ViewModel factories
    val loginViewModelFactory = ViewModelFactory<LoginViewModel> {
        LoginViewModel(authRepository)
    }
    val registerViewModelFactory = ViewModelFactory<RegisterViewModel> {
        RegisterViewModel(authRepository)
    }
    val homeViewModelFactory = ViewModelFactory<HomeViewModel> {
        HomeViewModel(indexRepository)
    }
    val storyViewModelFactory = ViewModelFactory<StoryViewModel> {
        StoryViewModel(storyRepository)
    }
}
