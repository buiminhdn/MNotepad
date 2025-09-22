package com.example.mnotepad.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mnotepad.database.AppDatabase
import com.example.mnotepad.database.DAO.NoteDao
import com.example.mnotepad.entities.models.Note
import com.example.mnotepad.helpers.SORT_CREATE_DATE_FROM_NEWEST
import com.example.mnotepad.helpers.SORT_CREATE_DATE_FROM_OLDEST
import com.example.mnotepad.helpers.SORT_EDIT_DATE_FROM_NEWEST
import com.example.mnotepad.helpers.SORT_EDIT_DATE_FROM_OLDEST
import com.example.mnotepad.helpers.SORT_TITLE_A_Z
import com.example.mnotepad.helpers.SORT_TITLE_Z_A
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao: NoteDao = AppDatabase.getDatabase(application).getNoteDao()

    // raws data from db
    val notes: LiveData<List<Note>> = noteDao.getAll()
    val deletedNotes: LiveData<List<Note>> = noteDao.getDeletedNotes()

    // UI data đã qua filter/sort
    private val _filteredNotes = MutableLiveData<List<Note>>()
    val filteredNotes: LiveData<List<Note>> get() = _filteredNotes

    private var currentNotes: List<Note> = emptyList()


    init {
        notes.observeForever { list ->
            currentNotes = list
            _filteredNotes.value = list
        }
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

    fun updateNotes(notes: List<Note>) = viewModelScope.launch (Dispatchers.IO) {
        noteDao.updateAll(notes.map { it.copy(updatedAt = System.currentTimeMillis()) })
    }

    fun filterByQuery(query: String) {
        _filteredNotes.value =
            if (query.isBlank()) currentNotes
            else currentNotes.filter { it.title.contains(query, ignoreCase = true) }
    }

    fun filterByCategory(categoryId: Int) {
        _filteredNotes.value =
            if (categoryId <= 0) currentNotes
            else currentNotes.filter { it.categoryIds?.contains(categoryId) ?: false }
    }

    fun sortBy(type: String) {
        _filteredNotes.value = when (type) {
            SORT_EDIT_DATE_FROM_NEWEST -> currentNotes.sortedByDescending { it.updatedAt }
            SORT_EDIT_DATE_FROM_OLDEST -> currentNotes.sortedBy { it.updatedAt }
            SORT_TITLE_A_Z -> currentNotes.sortedBy { it.title }
            SORT_TITLE_Z_A -> currentNotes.sortedByDescending { it.title }
            SORT_CREATE_DATE_FROM_NEWEST -> currentNotes.sortedByDescending { it.createdAt }
            SORT_CREATE_DATE_FROM_OLDEST -> currentNotes.sortedBy { it.createdAt }
            else -> currentNotes
        }
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