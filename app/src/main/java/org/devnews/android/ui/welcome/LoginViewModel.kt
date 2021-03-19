package org.devnews.android.ui.welcome

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.base.ViewModel
import org.devnews.android.repository.AuthRepository
import org.devnews.android.repository.getError
import org.devnews.android.repository.wrapAPIError
import retrofit2.HttpException

class LoginViewModel constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _loggedIn = MutableLiveData(false)
    private val _token = MutableLiveData<String?>()

    // Two way data binding with layout
    val username = MutableLiveData("")
    val password = MutableLiveData("")

    // Read-only live data
    val loggedIn: LiveData<Boolean> = _loggedIn
    val token: LiveData<String?> = _token

    /**
     * Validates the value of the username, and returns a string resource as error if it fails
     * validation.
     */
    fun validateUsername(): Int? {
        if (TextUtils.isEmpty(username.value)) {
            return R.string.username_empty
        }

        return null
    }

    /**
     * Validates the value of the password, and returns a string resource as error if it fails
     * validation.
     */
    fun validatePassword(): Int? {
        if (TextUtils.isEmpty(password.value)) {
            return R.string.password_empty
        }

        return null
    }

    /**
     * Returns whether the login data is valid.
     */
    fun isValid(): Boolean {
        var status = true
        status = (validateUsername() == null) && status
        status = (validatePassword() == null) && status
        return status
    }

    fun loginUser(context: Context) {
        _loggedIn.value = false
        Log.d(TAG, "Logging in the user, username: " + username.value)

        _loading.value = true
        viewModelScope.launch {
            _error.value = wrapAPIError(context, {
                when (it) {
                    403 -> context.getString(R.string.error_invalid_credentials)
                    else -> null
                }
            }) {
                val authResponse = authRepository.getAccessToken(username.value!!, password.value!!)
                Log.d(TAG, "Login success")

                _token.value = authResponse.accessToken
                _loggedIn.value = true
            }

            _loading.value = false
        }
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}