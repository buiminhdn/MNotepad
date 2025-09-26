package com.example.mnotepad.callbacks

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.mnotepad.activities.LockActivity
import com.example.mnotepad.database.PasswordStorage.getLastestTime
import com.example.mnotepad.database.PasswordStorage.getUnlockTime
import com.example.mnotepad.database.PasswordStorage.setLastestTime
import com.example.mnotepad.helpers.DateTimeHelper
import java.util.concurrent.TimeUnit

class ActivityLifecycleHandler(private val application: Application) :
    Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(p0: Activity) {
        Log.d(TAG, "onActivityPaused at ${p0.localClassName}")
    }

    override fun onActivityStarted(p0: Activity) {
        val currentTime = DateTimeHelper.getCurrentTime()
        val lastestTime = getLastestTime(application)
        val lockTime = getUnlockTime(application) ?: 10

//        val distance = TimeUnit.MILLISECONDS.toMinutes(currentTime - lastestTime)
//        val isLocked = ( distance - lockTime ) >= 0

        // Không khác gì mấy nhưng chuẩn từng li từng tí hơn 😂
        val distance = currentTime - lastestTime
        val isLocked = distance - TimeUnit.MINUTES.toMillis(lockTime.toLong()) > 0

        if (isLocked) {
            setLastestTime(application, currentTime)
            val intent = Intent(application, LockActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent)
        }
    }

    override fun onActivityDestroyed(p0: Activity) {
        Log.d(TAG, "onActivityDestroyed at ${p0.localClassName}")
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        Log.d(TAG, "onActivitySaveInstanceState at ${p0.localClassName}")
    }

    override fun onActivityStopped(p0: Activity) {
        Log.d(TAG, "onActivityStopped at ${p0.localClassName}")
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        Log.d(TAG, "onActivityCreated at ${p0.localClassName}")
    }

    override fun onActivityResumed(p0: Activity) {
        Log.d(TAG, "onActivityResumed at ${p0.localClassName}")
    }

    companion object {
        private const val TAG = "LifecycleCallbacks"
    }
}