package org.devnews.android.ui.welcome

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.devnews.android.R
import org.devnews.android.base.ViewModel
import org.devnews.android.repository.AuthRepository
import org.devnews.android.repository.wrapAPIError

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _registered = MutableLiveData(false)
    private val _token = MutableLiveData("")

    // Two-way data binding with layout
    val username = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val verify = MutableLiveData("")
    // Read-only live data
    val registered: LiveData<Boolean> = _registered
    val token: LiveData<String> = _token

    /**
     * Send a request to register a new user, and get an access token.
     */
    fun register(context: Context) {
        if (!validate(context))
            return

        _loading.value = true

        viewModelScope.launch {
            _error.value = wrapAPIError(context) {
                val username = username.value!!
                val password = password.value!!
                val email = email.value!!

                val authResponse = authRepository.registerUser(username, password, email)
                _token.value = authResponse.accessToken
                _registered.value = true
            }

            _loading.value = false
        }
    }

    fun validate(context: Context): Boolean {
        var ret = true
        ret = validateUsername(context) == null && ret
        ret = validateEmail(context, true) == null && ret
        ret = validatePassword(context, true) == null && ret
        ret = validateVerify(context) == null && ret
        return ret
    }

    fun validateUsername(context: Context): String? = when {
        TextUtils.isEmpty(username.value) ->
            context.getString(R.string.register_username_empty)
        else -> null
    }

    fun validateEmail(context: Context, full: Boolean = false): String? = when {
        full && !email.value!!.matches(Regex("""^\S+@\S+\.\S+$""")) ->
            context.getString(R.string.register_email_invalid)
        TextUtils.isEmpty(email.value) ->
            context.getString(R.string.register_email_empty)
        else -> null
    }

    fun validatePassword(context: Context, full: Boolean = false): String? = when {
        full && password.value!!.length < 8 ->
            context.getString(R.string.register_password_minimum)
        TextUtils.isEmpty(password.value) ->
            context.getString(R.string.register_password_empty)
        else -> null
    }

    fun validateVerify(context: Context): String? = when {
        password.value != verify.value ->
            context.getString(R.string.register_verify_match)
        else -> null
    }
}