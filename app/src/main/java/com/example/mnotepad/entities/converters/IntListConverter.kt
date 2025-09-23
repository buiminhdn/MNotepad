package com.example.mnotepad.entities.converters

import androidx.room.TypeConverter

class IntListConverter {
    @TypeConverter
    fun fromListInt(list: List<Int>?): String {
        if (list == null) return ""
        return list.joinToString(",")
    }

    @TypeConverter
    fun toListInt(data: String?): List<Int> {
        if (data == "") return emptyList()
        return listOf(*data?.split(",")?.map { it.toInt() }?.toTypedArray() as Array<out Int>)
    }
}