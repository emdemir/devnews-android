package org.devnews.android.account

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.os.bundleOf
import com.auth0.android.jwt.JWT
import kotlinx.coroutines.runBlocking
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.repository.AuthRepository
import retrofit2.HttpException
import java.lang.IllegalStateException


class DevNewsAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {
    companion object {
        const val ACCOUNT_TYPE = "org.devnews"
        const val AUTHTOKEN_ACCESS = "access"
        const val AUTHTOKEN_REFRESH = "refresh"
        const val AUTHTOKEN_IDENTITY = "identity"

        // Who is the originator of this account? google: Google Sign-in, local: Regular DevNews
        // account (no 3rd-party).
        const val KEY_ACCOUNT_SOURCE = "ACCOUNT_SOURCE"
        const val ACCOUNT_SOURCE_GOOGLE = "google"
        const val ACCOUNT_SOURCE_LOCAL = "devnews"

        private const val TAG = "DevNewsAuthenticator"
    }

    /**
     * Get the access token for the given account.
     */
    private suspend fun getAccessToken(
        repo: AuthRepository,
        manager: AccountManager,
        account: Account
    ): String? {
        Log.d(
            TAG, "Access token requested, trying to refresh via refresh token " +
                    "first."
        )

        var token: String? = null

        val refreshToken = manager.peekAuthToken(account, AUTHTOKEN_REFRESH)
        if (refreshToken != null) {
            Log.d(TAG, "Got valid refresh token, refreshing...")
            try {
                val authResponse = repo.refreshAccessToken(refreshToken)
                Log.d(TAG, "Successfully got the access token!")

                authResponse.refreshToken?.let {
                    Log.d(TAG, "Got refresh token, setting it in AccountManager")
                    manager.setAuthToken(account, AUTHTOKEN_REFRESH, it)
                }

                authResponse.accessToken?.let {
                    token = it
                }
            } catch (e: HttpException) {
                Log.w(TAG, "Got an HTTP error while trying to refresh token...", e)
            }
        }

        // If we were able to successfully fetch with the refresh token, exit.
        if (token != null) return token

        // Okay, time to try to re-authenticate. Use the token/password given to us by the user
        // (or the 3rd party authenticator) with the server.
        val source = manager.getUserData(account, KEY_ACCOUNT_SOURCE)
            ?: throw IllegalStateException("Didn't get account source?!")
        val password = manager.getPassword(account)
        if (password != null) {
            Log.d(TAG, "Getting access token from server...")
            try {
                val authResponse = repo.getAccessToken(account.name, password, source)
                if (authResponse.accessToken == null)
                    throw IllegalStateException("Didn't get access token?!")

                Log.d(TAG, "Successfully got the access token!")

                authResponse.refreshToken?.let {
                    Log.d(TAG, "Got refresh token, setting it in AccountManager")
                    manager.setAuthToken(account, AUTHTOKEN_REFRESH, it)
                }

                token = authResponse.accessToken
            } catch (e: HttpException) {
                Log.w(TAG, "Got an HTTP error while trying to fetch the access token...", e)
            }
        }

        return token
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        Log.d(TAG, "getToken()")
        // First, check whether we already have a token in cache
        val manager = AccountManager.get(context)
        var token = manager.peekAuthToken(account, authTokenType)
        Log.d(TAG, "Token exists in cache: ${token != null}")

        if (token == null || JWT(token).isExpired(0)) {
            token = null
            // Try to authenticate the user
            val repo = (context.applicationContext as DevNews).container.authRepository

            runBlocking {
                when (authTokenType) {
                    AUTHTOKEN_ACCESS -> {
                        token = getAccessToken(repo, manager, account)
                    }
                    AUTHTOKEN_IDENTITY -> {
                        Log.d(TAG, "Identity token requested, trying to fetch with access token.")
                        var accessToken = manager.peekAuthToken(account, AUTHTOKEN_ACCESS)

                        if (accessToken == null) {
                            Log.d(TAG, "Access token wasn't in cache, trying to get it")
                            // If we didn't get the access token even after this, then there's
                            // no way we can get it with current information.
                            val newAccessToken = getAccessToken(repo, manager, account)
                                ?: return@runBlocking
                            Log.d(TAG, "Access token obtained. Setting it in AccountManager")
                            manager.setAuthToken(account, AUTHTOKEN_ACCESS, accessToken)
                            accessToken = newAccessToken
                        }

                        Log.d(TAG, "Now obtaining identity token!")

                        try {
                            val authResponse = repo.getIdentityToken(accessToken)
                            if (authResponse.identityToken == null)
                                return@runBlocking

                            Log.d(TAG, "Got the identity token!")
                            manager.setAuthToken(account, AUTHTOKEN_IDENTITY, token)
                            token = authResponse.identityToken

                            // Check if the username of the account that's saved on the device matches
                            // the one that was sent back to us.
                            val tokenJWT = JWT(authResponse.identityToken)
                            if (tokenJWT.subject != account.name) {
                                Log.d(
                                    TAG,
                                    "Updating the account name with the one from the server."
                                )
                                updateAccount(
                                    context,
                                    tokenJWT.subject!!,
                                    manager.getPassword(account),
                                    accessToken,
                                    account.name
                                )
                            }
                        } catch (e: HttpException) {
                            Log.w(
                                TAG,
                                "Got an HTTP exception while trying to fetch the identity token...",
                                e
                            )
                        }
                    }
                    else -> {
                        throw IllegalArgumentException("Invalid authentication token type.")
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(token)) {
            Log.d(TAG, "Got the token successfully")
            return bundleOf(
                AccountManager.KEY_ACCOUNT_NAME to account.name,
                AccountManager.KEY_ACCOUNT_TYPE to account.type,
                AccountManager.KEY_AUTHTOKEN to token
            )
        }

        Log.d(TAG, "Could not fetch token from cache OR remotely! Asking user for credentials.")
        val intent = Intent(context, AddAccountActivity::class.java).apply {
            putExtra(AddAccountActivity.KEY_TITLE, context.getString(R.string.relogin_title))
            putExtra(AddAccountActivity.KEY_DETAILS, context.getString(R.string.relogin_info))
            putExtra(AddAccountActivity.KEY_USERNAME, account.name)
            putExtra(AddAccountActivity.KEY_ADDING_NEW_ACCOUNT, false)
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        }
        return bundleOf(
            AccountManager.KEY_INTENT to intent
        )
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        Log.d(TAG, "addAccount()")
        if (accountType != ACCOUNT_TYPE) {
            return bundleOf(AccountManager.KEY_ERROR_MESSAGE to context.getString(R.string.unknown_account_type))
        }

        Log.d(TAG, "Sending user to account add activity")

        // Send the user to our account activity.
        val intent = Intent(context, AddAccountActivity::class.java).apply {
            putExtra(AddAccountActivity.KEY_ACCOUNT_TYPE, accountType)
            putExtra(AddAccountActivity.KEY_ADDING_NEW_ACCOUNT, true)
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        }
        return bundleOf(AccountManager.KEY_INTENT to intent)
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse,
        account: Account,
        features: Array<out String>?
    ): Bundle {
        Log.d(TAG, "hasFeatures()")
        // This is only called when we want to see whether this authenticator has any extra
        // features. We don't expect this to get called, so we can say false to everything.
        return bundleOf(AccountManager.KEY_BOOLEAN_RESULT to false)
    }

    // ---

    override fun confirmCredentials(
        p0: AccountAuthenticatorResponse,
        p1: Account,
        p2: Bundle?
    ): Bundle? {
        Log.d(TAG, "confirmCredentials()")
        return null
    }

    override fun getAuthTokenLabel(authTokenType: String?): String? {
        // We do not support multiple authToken types.
        Log.d(TAG, "getAuthTokenLabel()")
        return null
    }

    override fun editProperties(p0: AccountAuthenticatorResponse?, p1: String?): Bundle? {
        Log.d(TAG, "editProperties()")
        return null
    }


    override fun updateCredentials(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: String?,
        p3: Bundle?
    ): Bundle? {
        Log.d(TAG, "updateCredentials()")
        return null
    }
}
