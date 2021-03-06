package org.devnews.android.account

import android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Service that provides the authenticator for Android.
 * This service doesn't actually do anything on its own; it only sends the authenticator
 * when the system asks for it.
 */
class AccountService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        if (intent?.action == ACTION_AUTHENTICATOR_INTENT) {
            return DevNewsAuthenticator(this).iBinder
        }

        return null
    }
}