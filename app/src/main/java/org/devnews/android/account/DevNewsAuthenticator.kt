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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.devnews.android.DevNews
import org.devnews.android.R
import retrofit2.HttpException
import kotlin.coroutines.coroutineContext


class DevNewsAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {
    companion object {
        const val ACCOUNT_TYPE = "org.devnews"
        const val AUTHTOKEN_TYPE = "default"
        private const val TAG = "DevNewsAuthenticator"
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
            // Probably not so great... but couldn't figure out a better way.
            val password = manager.getPassword(account)
            if (password != null) {
                Log.d(TAG, "Getting token from server...")
                runBlocking {
                    try {
                        token = repo.getToken(account.name, password)
                    } catch (e: HttpException) {
                        Log.d(TAG, "Got an HTTP error, code: ${e.code()}")
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(token)) {
            Log.d(TAG, "Got the token")
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
        val result = bundleOf(AccountManager.KEY_INTENT to intent)
        return result
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse,
        account: Account,
        features: Array<out String>?
    ): Bundle {
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
        return null
    }

    override fun getAuthTokenLabel(authTokenType: String?): String? {
        // We do not support multiple authToken types.
        return null
    }

    override fun editProperties(p0: AccountAuthenticatorResponse?, p1: String?): Bundle? {
        return null
    }


    override fun updateCredentials(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: String?,
        p3: Bundle?
    ): Bundle? {
        return null
    }
}
