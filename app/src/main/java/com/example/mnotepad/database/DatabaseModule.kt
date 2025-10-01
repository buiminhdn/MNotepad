package com.example.mnotepad.database

import android.content.Context
import androidx.room.Room
import com.example.mnotepad.database.DAO.CategoryDao
import com.example.mnotepad.database.DAO.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): NoteDatabase {
        return Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            "notepad_database"
        )
            .build()
    }

    @Provides
    fun provideCategoryDao(database: NoteDatabase): CategoryDao {
        return database.getCategoryDao()
    }

    @Provides
    fun provideNoteDao(database: NoteDatabase): NoteDao {
        return database.getNoteDao()
    }
}