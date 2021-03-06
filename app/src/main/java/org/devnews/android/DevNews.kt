package org.devnews.android

import android.app.Activity
import android.app.Application

class DevNews : Application() {
    var currentActivity: Activity? = null
    val container = AppContainer(this)
}