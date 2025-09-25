package com.example.mnotepad.database

import android.content.Context
import androidx.core.content.edit
import com.example.mnotepad.helpers.PREFS_NAME

object PasswordStorage {
    private const val KEY_PASSWORD = "app_password"
    private const val KEY_RECOVERY_EMAIL = "app_email"
    private const val KEY_UNLOCK_TIME = "app_unlock"
    private const val KEY_IS_LOCKED = "app_isLocked"

    fun getIsLocked(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isLocked = prefs.getBoolean(KEY_IS_LOCKED, false)
        return isLocked;
    }

    fun setIsLocked(context: Context, isLocked: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_IS_LOCKED, isLocked)
            apply()
        }
    }

    fun getUnlockTime(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val period = prefs.getInt(KEY_UNLOCK_TIME, 10)
        return period;
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
        return password;
    }

    fun isSetPassword(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val password = prefs.getString(KEY_PASSWORD, "")
        return password != "";
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
        return password;
    }

    fun setRecoveryEmail(context: Context, password: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_RECOVERY_EMAIL, password)
            apply()
        }
    }
}