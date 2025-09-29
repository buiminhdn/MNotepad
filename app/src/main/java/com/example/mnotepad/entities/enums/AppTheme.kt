package com.example.mnotepad.entities.enums

import androidx.annotation.StyleRes
import com.example.mnotepad.R
import com.example.mnotepad.helpers.THEME_BLACK_ID
import com.example.mnotepad.helpers.THEME_BROWN_ID
import com.example.mnotepad.helpers.THEME_GREY_ID
import com.example.mnotepad.helpers.THEME_SYSTEM_ID
import com.example.mnotepad.helpers.THEME_YELLOW_ID

enum class AppTheme(val id: Int, @StyleRes val styleRes: Int, val displayName: String) {
    BROWN(THEME_BROWN_ID, R.style.Theme_MNotepad_Brown, "Light"),
    BLACK(THEME_BLACK_ID, R.style.Theme_MNotepad_Black, "Dark"),
    YELLOW(THEME_YELLOW_ID, R.style.Theme_MNotepad_Yellow, "Solarized"),
    GREY(THEME_GREY_ID, R.style.Theme_MNotepad_Grey, "White"),
    SYSTEM(THEME_SYSTEM_ID, R.style.Theme_MNotepad_System, "Solarized Dark");

    companion object {
        fun fromId(id: Int): AppTheme = AppTheme.entries.firstOrNull { it.id == id } ?: BROWN
    }
}