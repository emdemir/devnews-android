package org.devnews.android

import okhttp3.OkHttpClient
import org.devnews.android.api.*
import org.devnews.android.home.home.HomeViewModel
import org.devnews.android.story.StoryViewModel
import org.devnews.android.tag.TagViewModel
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
    private val commentService = retrofit.create(CommentService::class.java)
    private val tagService = retrofit.create(TagService::class.java)

    // Repositories
    val authRepository = AuthRepository(authService)
    private val indexRepository = IndexRepository(indexService)
    private val storyRepository = StoryRepository(storyService)
    private val commentRepository = CommentRepository(commentService)
    private val tagRepository = TagRepository(tagService)

    // ViewModel factories
    val loginViewModelFactory = ViewModelFactory<LoginViewModel> {
        LoginViewModel(authRepository)
    }
    val registerViewModelFactory = ViewModelFactory<RegisterViewModel> {
        RegisterViewModel(authRepository)
    }
    val homeViewModelFactory = ViewModelFactory<HomeViewModel> {
        HomeViewModel(indexRepository, storyRepository)
    }
    val storyViewModelFactory = ViewModelFactory<StoryViewModel> {
        StoryViewModel(storyRepository, commentRepository)
    }
    val tagViewModelFactory = ViewModelFactory<TagViewModel> {
        TagViewModel(tagRepository)
    }
}
