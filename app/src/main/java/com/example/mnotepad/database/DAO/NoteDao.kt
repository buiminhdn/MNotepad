package com.example.mnotepad.database.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.entities.models.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAll(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedNotes(): LiveData<List<Note>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<Note>)

    @Update
    suspend fun update(note: Note)

    @Update
    suspend fun updateAll(notes: List<Note>)

    @Query("UPDATE notes SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Int)

    @Query("UPDATE notes SET isDeleted = 1 WHERE id IN (:ids)")
    suspend fun softDeleteNotes(ids: List<Int>)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun deleteAll()

    @Query("UPDATE notes SET isDeleted = 0 WHERE isDeleted = 1")
    suspend fun undeleteAll()

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNoteByIdSync(id: Int): Note?
}