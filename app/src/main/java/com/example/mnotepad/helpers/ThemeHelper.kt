package com.example.mnotepad.helpers

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit

object ThemeHelper {
    //    private var currentTheme = BROWN_THEME

    fun getTheme(context: Context): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_THEME, BROWN_THEME)
    }

    fun switchTheme(newTheme: Int, context: Context) {
        when (newTheme) {
            BROWN_THEME -> BROWN_THEME
            BLACK_THEME -> BLACK_THEME
            YELLOW_THEME -> YELLOW_THEME
            GREY_THEME -> GREY_THEME
            SYSTEM_THEME -> SYSTEM_THEME
            else -> -1
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putInt(
                KEY_THEME,
                newTheme
            )
        }
    }
}