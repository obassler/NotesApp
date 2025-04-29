package com.osu.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class NoteManager : ViewModel() {
    private val _notes = mutableStateListOf<Note>()
    val notes: List<Note> get() = _notes

    var draggedNoteId by mutableStateOf<String?>(null)

    fun addNote(note: Note) {
        _notes.add(note)
    }

    fun deleteNote(noteId: String) {
        _notes.removeAll { it.id == noteId }
    }

    fun updateNote(updatedNote: Note) {
        val index = _notes.indexOfFirst { it.id == updatedNote.id }
        if (index != -1) {
            _notes[index] = updatedNote.copy(modifiedAt = System.currentTimeMillis())
        }
    }

    fun moveNote(fromId: String, toId: String) {
        val fromIndex = _notes.indexOfFirst { it.id == fromId }
        val toIndex = _notes.indexOfFirst { it.id == toId }
        if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
            val item = _notes.removeAt(fromIndex)
            _notes.add(if (toIndex > fromIndex) toIndex - 1 else toIndex, item)
        }
    }
}
