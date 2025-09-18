package com.example.mnotepad.database.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mnotepad.entities.models.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAll(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedNotes(): LiveData<List<Note>>


    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Query("UPDATE notes SET isDeleted = 1 WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun deleteAll()

    @Query("UPDATE notes SET isDeleted = 0 WHERE isDeleted = 1")
    suspend fun undeleteAll()
}