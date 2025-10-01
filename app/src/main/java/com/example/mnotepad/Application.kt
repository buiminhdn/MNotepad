package com.example.mnotepad

import android.app.Application
import com.example.mnotepad.callbacks.ActivityLifecycleHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(ActivityLifecycleHandler(this))
    }
}
