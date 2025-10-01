package com.example.mnotepad.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mnotepad.database.DAO.CategoryDao
import com.example.mnotepad.database.DAO.NoteDao
import com.example.mnotepad.entities.converters.IntListConverter
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.entities.models.Note

@Database(entities = [Note::class, Category::class], version = 1)
@TypeConverters(IntListConverter::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun getCategoryDao(): CategoryDao
    abstract fun getNoteDao(): NoteDao
}