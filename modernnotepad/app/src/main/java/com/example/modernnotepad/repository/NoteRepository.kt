package com.example.modernnotepad.repository

import androidx.lifecycle.LiveData
import com.example.modernnotepad.dao.NoteDao
import com.example.modernnotepad.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository(private val noteDao: NoteDao) {
    val allNotesLiveData: LiveData<List<Note>> = noteDao.getAllNotes()

    suspend fun insertNote(note: Note): Long {
        return withContext(Dispatchers.IO) {
            noteDao.insertNote(note)
        }
    }

    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.updateNote(note)
        }
    }

    suspend fun deleteNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(note)
        }
    }

    suspend fun getNoteById(noteId: Long): Note? {
        return withContext(Dispatchers.IO) {
            noteDao.getNoteById(noteId)
        }
    }

    // 搜索方法
    fun searchNotes(query: String): LiveData<List<Note>> {
        return noteDao.searchNotes("%$query%")
    }

    // 分类相关方法
    fun getAllCategories(): LiveData<List<String>> = noteDao.getAllCategories()

    fun getNotesByCategory(category: String): LiveData<List<Note>> = noteDao.getNotesByCategory(category)

    fun getAllTags(): LiveData<List<String>> = noteDao.getAllTags()

    // 置顶相关
    suspend fun updatePinStatus(noteId: Long, isPinned: Boolean) {
        withContext(Dispatchers.IO) {
            noteDao.updatePinStatus(noteId, isPinned)
        }
    }

    // 颜色相关
    fun getNotesByColor(color: Int): LiveData<List<Note>> = noteDao.getNotesByColor(color)

    // 删除分类相关
    suspend fun deleteNotesByCategory(category: String) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNotesByCategory(category)
        }
    }

    suspend fun getNoteCountByCategory(category: String): Int {
        return withContext(Dispatchers.IO) {
            noteDao.getNoteCountByCategory(category)
        }
    }

    suspend fun updateNotesCategory(oldCategory: String, newCategory: String) {
        withContext(Dispatchers.IO) {
            noteDao.updateNotesCategory(oldCategory, newCategory)
        }
    }

    // 提醒相关
    fun getUpcomingReminders(currentTime: Long): LiveData<List<Note>> = noteDao.getUpcomingReminders(currentTime)
}