package com.example.mnotepad.entities.converters

import androidx.room.TypeConverter
import com.example.mnotepad.entities.models.ChecklistItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChecklistConverter {
    @TypeConverter
    fun fromChecklist(items: List<ChecklistItem>?): String {
        return Gson().toJson(items)
    }

    @TypeConverter
    fun toChecklist(json: String): List<ChecklistItem> {
        val type = object : TypeToken<List<ChecklistItem>>() {}.type
        return Gson().fromJson(json, type)
    }
}