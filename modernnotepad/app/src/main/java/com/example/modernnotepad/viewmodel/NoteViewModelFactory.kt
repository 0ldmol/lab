package com.example.modernnotepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.modernnotepad.repository.NoteRepository

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("未知的 ViewModel 类")
    }
}