package com.example.mnotepad

import android.app.Application
import com.example.mnotepad.callbacks.ActivityLifecycleHandler

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(ActivityLifecycleHandler(this))
    }


}