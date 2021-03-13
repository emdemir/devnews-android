package org.devnews.android.base

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import org.devnews.android.DevNews

/**
 * This class is used to cover the fact that Android (as of API 30) still doesn't have a way to
 * get the current activity from the application object. So this workaround is used.
 */
open class Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as DevNews
        if (app.currentActivity == null)
            app.currentActivity = this
    }

    override fun onResume() {
        super.onResume()
        (application as DevNews).currentActivity = this
    }

    override fun onDestroy() {
        val app = application as DevNews
        if (app.currentActivity?.equals(this) == true) {
            app.currentActivity = null
        }

        super.onDestroy()
    }
}