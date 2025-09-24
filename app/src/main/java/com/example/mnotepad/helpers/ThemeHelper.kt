package com.example.mnotepad.helpers

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.example.mnotepad.entities.enums.AppTheme

object ThemeManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_THEME = "app_theme"

    fun getSavedTheme(context: Context): AppTheme {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_THEME, AppTheme.BROWN.id)
        return AppTheme.fromId(id)
    }

    fun applyTheme(activity: Activity) {
        val theme = getSavedTheme(activity)
        activity.setTheme(theme.styleRes)
    }

    fun setTheme(activity: Activity, appTheme: AppTheme) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_THEME, appTheme.id) }
        activity.recreate()
    }
}