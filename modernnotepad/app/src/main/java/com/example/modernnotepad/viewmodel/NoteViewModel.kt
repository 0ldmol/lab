package com.example.modernnotepad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modernnotepad.model.Note
import com.example.modernnotepad.repository.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    val allNotes: LiveData<List<Note>> = repository.allNotesLiveData

    fun insertNote(note: Note, onSuccess: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertNote(note)
            onSuccess(id)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    suspend fun getNoteById(noteId: Long): Note? {
        return repository.getNoteById(noteId)
    }

    // 搜索方法
    fun searchNotes(query: String): LiveData<List<Note>> {
        return repository.searchNotes(query)
    }

    // 分类相关方法
    val allCategories: LiveData<List<String>> = repository.getAllCategories()

    fun getNotesByCategory(category: String): LiveData<List<Note>> {
        return repository.getNotesByCategory(category)
    }

    val allTags: LiveData<List<String>> = repository.getAllTags()

    // 置顶相关
    fun togglePinStatus(note: Note) {
        viewModelScope.launch {
            repository.updatePinStatus(note.id, !note.isPinned)
        }
    }

    // 颜色相关
    fun getNotesByColor(color: Int): LiveData<List<Note>> {
        return repository.getNotesByColor(color)
    }

    // 删除分类相关
    fun deleteNotesByCategory(category: String) {
        viewModelScope.launch {
            repository.deleteNotesByCategory(category)
        }
    }

    suspend fun getNoteCountByCategory(category: String): Int {
        return repository.getNoteCountByCategory(category)
    }

    fun updateNotesCategory(oldCategory: String, newCategory: String) {
        viewModelScope.launch {
            repository.updateNotesCategory(oldCategory, newCategory)
        }
    }

    // 提醒相关
    fun getUpcomingReminders(currentTime: Long): LiveData<List<Note>> {
        return repository.getUpcomingReminders(currentTime)
    }
}