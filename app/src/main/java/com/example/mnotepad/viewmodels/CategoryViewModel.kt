package com.example.mnotepad.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.mnotepad.database.AppDatabase
import com.example.mnotepad.database.DAO.CategoryDao
import com.example.mnotepad.database.DAO.NoteDao
import com.example.mnotepad.entities.models.Category
import com.example.mnotepad.entities.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    val categories: LiveData<List<Category>>
    private val categoryDao: CategoryDao = AppDatabase.getDatabase(application).getCategoryDao()

    init {
        categories = categoryDao.getAll()
    }

    fun deleteCategory(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        categoryDao.delete(id)
    }

    fun updateCategory(note: Category) = viewModelScope.launch(Dispatchers.IO) {
        categoryDao.update(note)
    }

    fun addCategory(category: Category): Long {
//        viewModelScope.launch(Dispatchers.Default) {
//            categoryDao.insert(note)
//        }
        var categoryId: Long = 0
        viewModelScope.launch {
            categoryId = categoryDao.insert(category)
            // Use newRoomId here, e.g., update LiveData, log it, etc.
        }

        return categoryId
    }
}