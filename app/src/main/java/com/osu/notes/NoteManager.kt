package com.osu.notes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import com.osu.notes.Note

class NoteManager(private val context: Context) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val sharedPrefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)
    private val notesKey = "notes"

    init {
        loadNotes()
    }

    fun addNote(note: Note) {
        _notes.update { currentNotes -> (currentNotes + note).sortedByDescending { it.createdAt } }
        saveNotes()
    }

    fun deleteNote(noteId: String) {
        _notes.update { it.filterNot { note -> note.id == noteId } }
        saveNotes()
    }

    fun moveNote(fromNote: Note, toIndex: Int) {
        _notes.update { currentNotes ->
            val fromIndex = currentNotes.indexOf(fromNote)
            if (fromIndex == -1 || toIndex < 0 || toIndex >= currentNotes.size) {
                return@update currentNotes
            }
            val mutableList = currentNotes.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(toIndex, item)
            mutableList.toList()
        }
        saveNotes()
    }


    private fun saveNotes() {
        viewModelScope.launch {
            try {
                val notesJson = Json.encodeToString(_notes.value)
                sharedPrefs.edit().putString(notesKey, notesJson).apply()
            } catch (e: Exception) {
                println("Error saving notes: ${e.message}")
            }
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            val notesJson = sharedPrefs.getString(notesKey, null)
            if (notesJson != null) {
                try {
                    val loadedNotes = Json.decodeFromString<List<Note>>(notesJson)
                    _notes.value = loadedNotes
                } catch (e: Exception) {
                    println("Error loading notes: ${e.message}")
                    _notes.value = emptyList()
                }
            } else {
                _notes.value = emptyList()
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NoteManager::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NoteManager(context.applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}