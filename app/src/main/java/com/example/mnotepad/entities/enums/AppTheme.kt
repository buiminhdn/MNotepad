package com.example.mnotepad.entities.enums

import androidx.annotation.StyleRes
import com.example.mnotepad.R

enum class AppTheme(val id: Int, @StyleRes val styleRes: Int, val displayName: String) {
    BROWN(0, R.style.Theme_MNotepad_Brown, "Brown"),
    BLACK(1, R.style.Theme_MNotepad_Black, "Black"),
    YELLOW(2, R.style.Theme_MNotepad_Yellow, "Yellow"),
    GREY(3, R.style.Theme_MNotepad_Grey, "Grey"),
    SYSTEM(4, R.style.Theme_MNotepad_System, "System");

    companion object {
        fun fromId(id: Int): AppTheme = AppTheme.entries.firstOrNull { it.id == id } ?: BROWN
    }
}