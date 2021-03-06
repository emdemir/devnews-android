package org.devnews.android.welcome

import android.text.TextUtils
import android.util.Log
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.api.AuthRepository
import org.devnews.android.api.getError
import retrofit2.HttpException

class LoginViewModel constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _loggingIn = MutableLiveData(false)
    private val _loggedIn = MutableLiveData(false)
    private val _token = MutableLiveData<String?>()
    private val _errorCode = MutableLiveData(0)
    private val _errorMessage = MutableLiveData("")

    // Two way data binding with layout
    val username = MutableLiveData("")
    val password = MutableLiveData("")
    // Read-only live data
    val loggingIn: LiveData<Boolean> = _loggingIn
    val loggedIn: LiveData<Boolean> = _loggedIn
    val token: LiveData<String?> = _token
    val errorCode: LiveData<Int> = _errorCode
    val errorMessage: LiveData<String> = _errorMessage

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

    fun loginUser() {
        _loggingIn.value = true
        _loggedIn.value = false
        _errorCode.value = 0
        _errorMessage.value = ""
        Log.d(TAG, "Logging in the user, username: " + username.value)

        viewModelScope.launch {
            try {
                val token = authRepository.getToken(username.value!!, password.value!!)
                Log.d(TAG, "Login success")

                _token.value = token
                _loggingIn.value = false
                _loggedIn.value = true
            } catch (e: HttpException) {
                Log.e("LoginViewModel", "Login failure with HTTP error code", e)
                _loggingIn.value = false

                when (e.code()) {
                    400 -> {
                        Log.d(TAG, "Bad request, apparently.")
                        _errorMessage.value = getError(e.response()!!)
                    }
                    403 -> {
                        Log.d(TAG, "Invalid password")
                        _errorCode.value = R.string.error_invalid_credentials
                    }
                    else -> {
                        Log.d(TAG, "Unknown error")
                        _errorCode.value = R.string.error_unknown
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login failure!", e)
                _loggingIn.value = false
                _errorCode.value = R.string.error_unknown
            }
        }
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}