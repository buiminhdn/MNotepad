package com.example.mnotepad.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mnotepad.assets.OptionsData.colorPalette
import com.example.mnotepad.database.DAO.NoteDao
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.SORT_COLOR
import com.example.mnotepad.helpers.SORT_CREATE_DATE_FROM_NEWEST
import com.example.mnotepad.helpers.SORT_CREATE_DATE_FROM_OLDEST
import com.example.mnotepad.helpers.SORT_EDIT_DATE_FROM_NEWEST
import com.example.mnotepad.helpers.SORT_EDIT_DATE_FROM_OLDEST
import com.example.mnotepad.helpers.SORT_TITLE_A_Z
import com.example.mnotepad.helpers.SORT_TITLE_Z_A
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteDao: NoteDao,
    private val state: SavedStateHandle
) : ViewModel() {

    // raws data from db
    val notes: LiveData<List<Note>> = noteDao.getAll()
    val deletedNotes: LiveData<List<Note>> = noteDao.getDeletedNotes()

    // UI data đã qua filter/sort
    private val _filteredNotes = MutableLiveData<List<Note>>()
    val filteredNotes: LiveData<List<Note>> get() = _filteredNotes

    private var currentNotes: List<Note> = emptyList()

    private var currentCategoryId: Int
        get() = state["currentCategoryId"] ?: 0
        set(value) { state["currentCategoryId"] = value }

    private var currentSortType: String
        get() = state["currentSortType"] ?: SORT_EDIT_DATE_FROM_NEWEST
        set(value) { state["currentSortType"] = value }

    private var currentQuery: String
        get() = state["currentQuery"] ?: ""
        set(value) { state["currentQuery"] = value }


    init {
        notes.observeForever { list ->
            currentNotes = list
            applyFiltersAndSort()
        }
    }

    private fun applyFiltersAndSort() {
        var result = currentNotes

        // 1) filter by query
        if (currentQuery.isNotBlank()) {
            result = result.filter { it.title.contains(currentQuery, ignoreCase = true) }
        }

        // 2) filter by category
        result = when (currentCategoryId) {
            0 -> result // all
            -1 -> result.filter { it.categoryIds.isNullOrEmpty() || it.categoryIds == listOf(0) }
            else -> result.filter { it.categoryIds?.contains(currentCategoryId) ?: false }
        }

        // 3) sort the filtered result
        result = when (currentSortType) {
            SORT_EDIT_DATE_FROM_NEWEST -> result.sortedByDescending { it.updatedAt }
            SORT_EDIT_DATE_FROM_OLDEST -> result.sortedBy { it.updatedAt }
            SORT_TITLE_A_Z -> result.sortedBy { it.title }
            SORT_TITLE_Z_A -> result.sortedByDescending { it.title }
            SORT_CREATE_DATE_FROM_NEWEST -> result.sortedByDescending { it.createdAt }
            SORT_CREATE_DATE_FROM_OLDEST -> result.sortedBy { it.createdAt }
            SORT_COLOR -> result.sortedBy { note ->
                val index = colorPalette.indexOf(note.color)
                if (index == -1) Int.MAX_VALUE else index
            }
            else -> result
        }

        _filteredNotes.value = result
    }

    fun deleteNote(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        noteDao.softDelete(id)
    }

    fun upsertNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        if (note.id == 0) {
            noteDao.insert(note)
        } else {
            // keep createdAt, so use copy
            noteDao.update(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun insertNotes(notes: List<Note>) = viewModelScope.launch(Dispatchers.IO) {
        noteDao.insertAll(notes)
    }

    fun updateNotes(notes: List<Note>) = viewModelScope.launch(Dispatchers.IO) {
        noteDao.updateAll(notes.map { it.copy(updatedAt = System.currentTimeMillis()) })
    }

    fun filterByCategory(categoryId: Int) {
        currentCategoryId = categoryId
        applyFiltersAndSort()
    }

    fun filterByQuery(query: String) {
        currentQuery = query
        applyFiltersAndSort()
    }

    fun sortBy(type: String) {
        currentSortType = type
        applyFiltersAndSort()
    }

    fun filterByDeletedCategory(categoryId: Int): List<Note> {
        when (categoryId) {
            0 -> null // tất cả
            -1 -> null
            else -> return currentNotes.filter {
                it.categoryIds?.contains(categoryId) ?: false
            }
        }
        return emptyList()
    }
    fun softDeleteNotes(notes: List<Note>) = viewModelScope.launch(Dispatchers.IO) {
        noteDao.softDeleteNotes(notes.map { it.id })
    }

    fun deleteAllNotes() = viewModelScope.launch(Dispatchers.IO) {
        noteDao.deleteAll()
    }

    fun undeleteAllNotes() = viewModelScope.launch(Dispatchers.IO) {
        noteDao.undeleteAll()
    }
}
