package org.devnews.android.repository

import android.accounts.AccountManager
import android.content.Context
import android.util.Log
import androidx.annotation.CheckResult
import com.auth0.android.jwt.JWT
import com.google.gson.Gson
import okhttp3.Interceptor
import org.devnews.android.DevNews
import org.devnews.android.R
import org.devnews.android.account.getAccountDetails
import retrofit2.HttpException
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

                    Log.d(TAG, "tEST $response")

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
        // so no need to check for 401.
        return chain.proceed(newReq)
    }
}

/**
 * Wrap the call with a try/catch, and handle the common scenarios. The return value should be
 * assigned to an Observable error value.
 *
 * @param context Android context used to resolve strings.
 * @param httpCodeHandler If a HTTP error code other than 400 is received and this lambda is passed,
 * it will be run. If it returns a string, that string is used as the error value. Otherwise a generic
 * unknown error is generated.
 * @param fn The lambda to be run.
 * @return a string if there was an error, null otherwise.
 */
@CheckResult
suspend fun wrapAPIError(
    context: Context,
    httpCodeHandler: ((Int) -> String?)? = null,
    fn: suspend () -> Unit
): String? {
    try {
        fn()
    } catch (e: HttpException) {
        Log.d(TAG, "API operation received a HTTP exception.")

        when (e.code()) {
            400 -> {
                Log.d(TAG, "Bad request, apparently.")
                return getError(e.response()!!)
            }
            else -> {
                // Try the error code handler
                val err = httpCodeHandler?.invoke(e.code())

                return if (err == null) {
                    Log.d(TAG, "HTTP code handler doesn't exist or didn't handle it, returning generic error.")
                    context.getString(R.string.error_unknown)
                } else {
                    Log.d(TAG, "HTTP error code handler returned a string, passing it through.")
                    err
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Exception raised while running the API call!", e)
        return context.getString(R.string.error_unknown)
    }

    return null
}

private const val TAG = "ApiUtils"