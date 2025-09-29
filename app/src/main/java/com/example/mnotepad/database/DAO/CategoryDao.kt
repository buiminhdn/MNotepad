package com.example.mnotepad.database.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mnotepad.entities.models.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY orderIndex")
    fun getAll(): LiveData<List<Category>>

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: Int)

    @Update
    suspend fun updateAll(categories: List<Category>)
}