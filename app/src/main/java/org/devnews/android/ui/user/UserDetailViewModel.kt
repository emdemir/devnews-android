package org.devnews.android.ui.user

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.base.ViewModel
import org.devnews.android.repository.UserRepository
import org.devnews.android.repository.objects.User
import org.devnews.android.repository.wrapAPIError
import java.lang.IllegalStateException

class UserDetailViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _user = MutableLiveData<User?>()
    private val _username = MutableLiveData<String?>()

    val user: LiveData<User?> = _user

    /**
     * Set the username. Resets the user data.
     *
     * @param username The username
     */
    fun setUsername(username: String) {
        _user.value = null
        _username.value = username
    }

    /**
     * Load the user from the server.
     *
     * @param context Android context
     */
    fun loadUser(context: Context) {
        val username = _username.value ?: throw IllegalStateException("loadUser called with null user!")

        _error.value = null
        _loading.value = true

        viewModelScope.launch {
            _error.value = wrapAPIError(context, {
                when (it) {
                    404 -> context.getString(R.string.error_user_not_found)
                    else -> null
                }
            }) {
                Log.d("UserDetailViewModel", "Got here!")
                _user.value = userRepository.getUserDetails(username)
                Log.d("UserDetailViewModel", "Got here 2!")
            }

            _loading.value = false
        }
    }
}