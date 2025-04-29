package com.osu.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NoteListScreen(noteManager: NoteManager = viewModel()) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(noteManager.notes, key = { it.id }) { note ->
            NoteItem(
                note = note,
                isDragging = note.id == noteManager.draggedNoteId,
                onDragStart = { noteManager.draggedNoteId = note.id },
                onDragEnd = { noteManager.draggedNoteId = null },
                onDropOn = {
                    val draggedId = noteManager.draggedNoteId
                    if (draggedId != null && draggedId != note.id) {
                        noteManager.moveNote(draggedId, note.id)
                        noteManager.draggedNoteId = null
                    }
                }
            )
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDropOn: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(8.dp)
            .background(
                if (isDragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd(); onDropOn() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { change, _ -> change.consume() }
                )
            }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = note.title, style = MaterialTheme.typography.titleMedium)
            Text(text = note.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
