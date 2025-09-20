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
import kotlinx.coroutines.runBlocking

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    val categories: LiveData<List<Category>>
    private val categoryDao: CategoryDao = AppDatabase.getDatabase(application).getCategoryDao()

    init {
        categories = categoryDao.getAll()
    }

    fun deleteCategory(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        categoryDao.delete(id)
    }

    fun updateCategory(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        categoryDao.update(category)
    }

    fun updateCategories(categories: List<Category>) = viewModelScope.launch(Dispatchers.IO) {
        categoryDao.updateAll(categories)
    }

//    fun addCategory(category: Category): Long = runBlocking {
//        categoryDao.insert(category)
//    }

    suspend fun addCategory(category: Category): Long {
        return categoryDao.insert(category)
    }

    fun addCategoryWithOrder(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val category = Category(id = 0, name = name)
            val newId = categoryDao.insert(category)

            // update orderIndex = id
            categoryDao.update(
                category.copy(id = newId.toInt(), orderIndex = newId.toInt())
            )
        }
    }

}