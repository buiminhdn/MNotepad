package com.example.mnotepad.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.mnotepad.database.AppDatabase
import com.example.mnotepad.database.DAO.NoteDao
import com.example.mnotepad.entities.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    val notes: LiveData<List<Note>>
    private val noteDao: NoteDao = AppDatabase.getDatabase(application).getNoteDao()

    init {
        notes = noteDao.getAll()
    }

    fun deleteNote(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        noteDao.delete(id)
    }

    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        noteDao.update(note)
    }

    fun addNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        noteDao.insert(note)
    }
}