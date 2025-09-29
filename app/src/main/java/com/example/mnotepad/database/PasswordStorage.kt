package com.example.mnotepad.database

import android.content.Context
import androidx.core.content.edit
import com.example.mnotepad.helpers.DEFAULT_UNLOCK_TIME
import com.example.mnotepad.helpers.PREFS_NAME

object PasswordStorage {
    private const val KEY_PASSWORD = "app_password"
    private const val KEY_RECOVERY_EMAIL = "app_email"
    private const val KEY_UNLOCK_TIME = "app_unlock"
    private const val KEY_LASTEST_TIME = "app_lastest_time"

    fun getLastestTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LASTEST_TIME, 0)
    }

    fun setLastestTime(context: Context, time: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putLong(KEY_LASTEST_TIME, time)
            apply()
        }
    }

    fun getUnlockTime(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val period = prefs.getInt(KEY_UNLOCK_TIME, DEFAULT_UNLOCK_TIME)
        return period
    }

    fun setUnlockTime(context: Context, period: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putInt(KEY_UNLOCK_TIME, period)
            apply()
        }
    }

    fun getPassword(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val password = prefs.getString(KEY_PASSWORD, "")
        return password
    }

    fun isSetPassword(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val password = prefs.getString(KEY_PASSWORD, "")
        return password != ""
    }

    fun setPassword(context: Context, password: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_PASSWORD, password)
            apply()
        }
    }

    fun removePassword(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove(KEY_PASSWORD)
            apply()
        }
    }

    fun getRecoveryEmail(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val password = prefs.getString(KEY_RECOVERY_EMAIL, "")
        return password
    }

    fun setRecoveryEmail(context: Context, password: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_RECOVERY_EMAIL, password)
            apply()
        }
    }
}
