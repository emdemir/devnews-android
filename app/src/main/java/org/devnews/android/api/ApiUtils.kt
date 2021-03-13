package org.devnews.android.api

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.util.Log
import android.widget.Toast
import com.auth0.android.jwt.JWT
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import org.devnews.android.DevNews
import org.devnews.android.account.DevNewsAuthenticator.Companion.AUTHTOKEN_TYPE
import org.devnews.android.account.getAccountDetails
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.concurrent.locks.ReentrantLock

data class ErrorResponse(val error: String?, val errors: List<String>?)

/** Return the error for this {@link Response} object.
 *
 * @param response The response object.
 * @return The string for the response.
 * @throws IllegalArgumentException if the response had no error
 */
fun getError(response: Response<*>): String {
    response.errorBody()?.let { resp ->
        val error = Gson().fromJson(resp.charStream(), ErrorResponse::class.java)

        return error.error ?: error.errors!!.joinToString("\n")
    }
    throw IllegalArgumentException("Response contained no error body")
}

/**
 * Used in case of API errors.
 */
class APIError(message: String?) : Exception(message)

/**
 * Implements an interceptor that injects the token for the current account into each request.
 */
class TokenInterceptor(private val application: DevNews) : Interceptor {
    companion object {
        private var token: String? = null
        private val lock = ReentrantLock()
        const val TAG = "TokenInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        Log.d(TAG, "Intercepting request")
        val req = chain.request()

        if (req.url().encodedPath().startsWith("/auth")) {
            // Auth requests will either send username/password or token in body,
            // no need to add Authorization.
            Log.d(TAG, "Request is auth, not intercepting!")
            return chain.proceed(req)
        }

        // Get token if we don't have it

        val tok = try {
            lock.lock()
            token.run {
                if (this == null || JWT(this).isExpired(0)) {
                    Log.d(TAG, "Re-fetching token")
                    val activity = application.currentActivity
                        ?: throw IllegalStateException("Application has no activity but is somehow making a request?!")

                    val response = getAccountDetails(activity)
                    val error = response.getString(AccountManager.KEY_ERROR_MESSAGE)
                    if (error != null) {
                        // Apparently the token fetching failed. Possible culprits are
                        // 1) User changed their password elsewhere
                        // 2) The server is having issues
                        Log.w(TAG, "Got error while fetching token! $error")
                        throw APIError(error)
                    }

                    // Get the token
                    val tok = response.getString(AccountManager.KEY_AUTHTOKEN)
                        ?: throw IllegalStateException("No error but token is missing?!")

                    token = tok
                    tok
                } else {
                    this
                }
            }
        } finally {
            lock.unlock()
        }

        Log.d(TAG, "Fetched token, adding it to the request")

        // Add the token to the request
        val newReq = req.newBuilder()
            .header("Authorization", "Bearer $tok")
            .method(req.method(), req.body())
            .build()

        Log.d(TAG, "Continuing with the new request")

        // We can guarantee that we didn't use an expired token with the stuff above,
        // so no need to check for 401. (watch this bite me in the bum in a week)
        return chain.proceed(newReq)
    }

}