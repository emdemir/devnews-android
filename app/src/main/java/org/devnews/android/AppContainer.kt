package org.devnews.android

import okhttp3.OkHttpClient
import org.devnews.android.repository.*
import org.devnews.android.ui.home.comments.NewestCommentsViewModel
import org.devnews.android.ui.home.home.HomeViewModel
import org.devnews.android.ui.home.messages.MessageListViewModel
import org.devnews.android.ui.home.recent.RecentViewModel
import org.devnews.android.ui.message.thread.MessageThreadViewModel
import org.devnews.android.ui.story.create.StoryCreateViewModel
import org.devnews.android.ui.story.details.StoryDetailsViewModel
import org.devnews.android.ui.tag.TagViewModel
import org.devnews.android.ui.user.UserDetailViewModel
import org.devnews.android.utils.ViewModelFactory
import org.devnews.android.ui.welcome.LoginViewModel
import org.devnews.android.ui.welcome.RegisterViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Suppress("UNCHECKED_CAST")
class AppContainer(application: DevNews) {

    // Main retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.HOMESERVER)
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
    private val messageService = retrofit.create(MessageService::class.java)
    private val userService = retrofit.create(UserService::class.java)

    // Repositories
    val authRepository = AuthRepository(authService)
    private val indexRepository = IndexRepository(indexService)
    private val storyRepository = StoryRepository(storyService)
    private val commentRepository = CommentRepository(commentService)
    private val tagRepository = TagRepository(tagService)
    private val messageRepository = MessageRepository(messageService)
    private val userRepository = UserRepository(userService)

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
    val recentViewModelFactory = ViewModelFactory<RecentViewModel> {
        RecentViewModel(indexRepository, storyRepository)
    }
    val newestCommentsViewModelFactory = ViewModelFactory<NewestCommentsViewModel> {
        NewestCommentsViewModel(indexRepository, commentRepository)
    }
    val storyViewModelFactory = ViewModelFactory<StoryDetailsViewModel> {
        StoryDetailsViewModel(storyRepository, commentRepository)
    }
    val tagViewModelFactory = ViewModelFactory<TagViewModel> {
        TagViewModel(tagRepository, storyRepository)
    }
    val messageThreadViewModelFactory = ViewModelFactory<MessageThreadViewModel> {
        MessageThreadViewModel(messageRepository)
    }
    val messageListViewModelFactory = ViewModelFactory<MessageListViewModel> {
        MessageListViewModel(messageRepository)
    }
    val userDetailViewModelFactory = ViewModelFactory<UserDetailViewModel> {
        UserDetailViewModel(userRepository)
    }
    val storyCreateViewModelFactory = ViewModelFactory<StoryCreateViewModel> {
        StoryCreateViewModel(tagRepository, storyRepository)
    }
}