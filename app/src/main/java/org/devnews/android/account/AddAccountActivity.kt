package org.devnews.android.account

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.base.Activity
import org.devnews.android.ui.welcome.LoginViewModel

class AddAccountActivity : Activity() {
    // Subscribe to the login viewmodel to grab the credentials when the user finishes login
    private val loginViewModel: LoginViewModel by viewModels(factoryProducer = {
        (application as DevNews).container.loginViewModelFactory
    })

    init {
        Log.d(TAG, "Got here")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_account)

        // Set the application toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        // If a custom title was sent, update title
        intent.getStringExtra(KEY_TITLE)?.let {
            title = it
        }

        // If the username was sent, set it
        val initialUsername = intent.getStringExtra(KEY_USERNAME)
        initialUsername?.let {
            loginViewModel.username.value = it
        }

        // Grab the response object
        val response = intent.getParcelableExtra<AccountAuthenticatorResponse>(
            AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE
        )!!
        Log.d(TAG, "Authenticator response: $response")

        // When the viewmodel signals that the login is complete, grab the account details and
        // send them over to AccountManager, which will take care of storing the credentials.
        loginViewModel.loggedIn.observe(this) {
            if (it != true) return@observe

            Log.d(TAG, "User apparently logged in, add their account")

            val username = loginViewModel.username.value!!
            val password = loginViewModel.password.value!!
            val token = loginViewModel.token.value
            val source = loginViewModel.source.value!!

            // Launch a separate thread, because otherwise AccountManager will loudly
            // complain about doing things in the main thread.
            Thread {
                try {
                    if (intent.getBooleanExtra(KEY_ADDING_NEW_ACCOUNT, true)) {
                        Log.d(TAG, "Adding a new account")
                        addAccount(
                            this@AddAccountActivity,
                            username,
                            password,
                            source,
                            token = token
                        )
                    } else {
                        Log.d(TAG, "Updating existing account")
                        updateAccount(
                            this@AddAccountActivity,
                            username,
                            password,
                            token,
                            initialUsername,
                            newSource = source
                        )
                    }

                    // We've obtained the access token so far, but we also need to fetch the identity
                    // token which is required for all other requests.
                    val manager = AccountManager.get(this@AddAccountActivity)
                    val account = Account(username, DevNewsAuthenticator.ACCOUNT_TYPE)
                    val authBundle = manager.getAuthToken(
                        account,
                        DevNewsAuthenticator.AUTHTOKEN_IDENTITY,
                        Bundle(),
                        this@AddAccountActivity,
                        null,
                        null
                    ).result

                    response.onResult(
                        bundleOf(
                            AccountManager.KEY_ACCOUNT_NAME to username,
                            AccountManager.KEY_ACCOUNT_TYPE to DevNewsAuthenticator.ACCOUNT_TYPE,
                            AccountManager.KEY_AUTHTOKEN to authBundle.getString(AccountManager.KEY_AUTHTOKEN)
                        )
                    )
                    Log.d(TAG, "Added account, bye!")
                    finish()
                } catch (e: Exception) {
                    Log.e("AddAccountActivity", "Error when adding account", e)
                    response.onError(1, getString(R.string.error_unknown))
                }
            }.start()
        }
    }

    companion object {
        const val KEY_USERNAME = "USERNAME"
        const val KEY_ADDING_NEW_ACCOUNT = "ADDING_NEW_ACCOUNT"
        const val KEY_DETAILS = "DETAILS"
        const val KEY_TITLE = "TITLE"
        const val KEY_ACCOUNT_TYPE = "ACCOUNT_TYPE"
        private const val TAG = "AddAccountActivity"
    }
}
