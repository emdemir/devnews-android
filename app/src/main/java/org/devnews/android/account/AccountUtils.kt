package org.devnews.android.account

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import org.devnews.android.R

/**
 * Adds a new account to Android's AccountManager.
 * The account type will always be {@link DevNewsAuthenticator.ACCOUNT_TYPE}.
 *
 * @param context The application/activity context
 * @param username The account username
 * @param password The password for the account
 * @param token Optional, sets the access token if given
 */
fun addAccount(context: Context, username: String, password: String, token: String? = null) {
    val manager = AccountManager.get(context)
    val account = Account(username, DevNewsAuthenticator.ACCOUNT_TYPE)

    manager.addAccountExplicitly(account, password, bundleOf())
    if (token != null)
        manager.setAuthToken(account, DevNewsAuthenticator.AUTHTOKEN_ACCESS, token)
}

/**
 * Updates the password associated with the given DevNews account on the device.
 * The account type will always be {@link DevNewsAuthenticator.ACCOUNT_TYPE}.
 *
 * @param context The application/activity context
 * @param username The account username
 * @param password The password for the account
 * @param token Optional, sets the access token if given
 * @param initialUsername if set, and doesn't match username, will update account name
 */
fun updateAccount(
    context: Context, username: String, password: String, token: String? = null,
    initialUsername: String? = null
) {
    val manager = AccountManager.get(context)
    var account = Account(username, DevNewsAuthenticator.ACCOUNT_TYPE)

    if (initialUsername != null && initialUsername != username) {
        account = manager.renameAccount(
            Account(initialUsername, account.type),
            username,
            null,
            null
        ).result
    }

    manager.setPassword(account, password)
    if (token != null)
        manager.setAuthToken(account, DevNewsAuthenticator.AUTHTOKEN_ACCESS, token)
}

/**
 * Return an identity token for the DevNews account that is currently saved on the device.
 * The user is prompted to choose an account if they have more than one, and will be asked to create
 * an account if they have none.
 * This function should not be called from the main thread, because it will block while the
 * authentication token is being obtained.
 *
 * @param activity The activity that started the auth token request
 * @return The bundle as returned by the future, which contains the account name, type and token
 */
fun getAccountDetails(activity: Activity): Bundle {
    val manager = AccountManager.get(activity)
    val future = manager.getAuthTokenByFeatures(
        DevNewsAuthenticator.ACCOUNT_TYPE,
        DevNewsAuthenticator.AUTHTOKEN_IDENTITY,
        arrayOf(),
        activity,
        null,
        null,
        null,
        null
    )

    return future.result
}

/**
 * Return whether there's at least one DevNews account on this device.
 */
fun hasAccount(context: Context): Boolean {
    return AccountManager.get(context).getAccountsByType(DevNewsAuthenticator.ACCOUNT_TYPE)
        .isNotEmpty()
}