package com.example.modernnotepad.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.modernnotepad.model.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Long): Note?

    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // 搜索查询
    @Query("SELECT * FROM notes WHERE title LIKE :query OR content LIKE :query ORDER BY isPinned DESC, updatedAt DESC")
    fun searchNotes(query: String): LiveData<List<Note>>

    // 分类相关查询
    @Query("SELECT DISTINCT category FROM notes ORDER BY category")
    fun getAllCategories(): LiveData<List<String>>

    @Query("SELECT * FROM notes WHERE category = :category ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByCategory(category: String): LiveData<List<Note>>

    @Query("SELECT DISTINCT tags FROM notes WHERE tags != ''")
    fun getAllTags(): LiveData<List<String>>

    // 置顶相关
    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :noteId")
    suspend fun updatePinStatus(noteId: Long, isPinned: Boolean)

    // 颜色相关
    @Query("SELECT * FROM notes WHERE color = :color ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByColor(color: Int): LiveData<List<Note>>

    // 删除分类相关
    @Query("DELETE FROM notes WHERE category = :category")
    suspend fun deleteNotesByCategory(category: String)

    @Query("SELECT COUNT(*) FROM notes WHERE category = :category")
    suspend fun getNoteCountByCategory(category: String): Int

    @Query("UPDATE notes SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateNotesCategory(oldCategory: String, newCategory: String)

    // 提醒相关
    @Query("SELECT * FROM notes WHERE reminderTime IS NOT NULL AND reminderTime > :currentTime ORDER BY reminderTime ASC")
    fun getUpcomingReminders(currentTime: Long): LiveData<List<Note>>
}