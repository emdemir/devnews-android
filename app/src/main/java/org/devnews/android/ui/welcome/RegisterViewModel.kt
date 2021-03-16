package org.devnews.android.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.devnews.android.repository.AuthRepository

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _registering = MutableLiveData(false)
    private val _registered = MutableLiveData(false)
    private val _token = MutableLiveData("")

    // Two-way data binding with layout
    val username = MutableLiveData("")
    val email = MutableLiveData("")
    val password = MutableLiveData("")
    val repeat = MutableLiveData("")
    // Read-only live data
    val registering: LiveData<Boolean> = _registering
    val registered: LiveData<Boolean> = _registered
    val token: LiveData<String> = _token

    fun register() {

    }
}