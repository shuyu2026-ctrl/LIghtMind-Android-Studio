package com.example.lightmind.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lightmind.data.local.Note
import com.example.lightmind.data.local.NoteDatabase
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val noteDao = NoteDatabase.getDatabase(application).noteDao()

    private val _currentNote = MutableLiveData<Note?>()
    val currentNote: LiveData<Note?> = _currentNote

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    fun loadNote(noteId: Int) {
        viewModelScope.launch {
            val note = noteDao.getNoteById(noteId)
            _currentNote.value = note
        }
    }

    fun saveNote(noteId: Int, title: String, content: String) {
        viewModelScope.launch {
            try {
                if (noteId == -1) {
                    val note = Note(
                        title = title,
                        content = content
                    )
                    noteDao.insertNote(note)
                } else {
                    val note = Note(
                        id = noteId,
                        title = title,
                        content = content,
                        createdAt = _currentNote.value?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    noteDao.updateNote(note)
                }
                _saveResult.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _saveResult.value = false
            }
        }
    }
}