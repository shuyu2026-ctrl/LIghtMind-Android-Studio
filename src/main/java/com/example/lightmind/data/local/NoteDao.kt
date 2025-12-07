package com.example.lightmind.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?

    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%'")
    suspend fun searchNotes(keyword: String): List<Note>

    @Query("SELECT * FROM notes ORDER BY updated_at DESC LIMIT :limit")
    suspend fun getRecentNotes(limit: Int = 20): List<Note>
}