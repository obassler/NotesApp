package com.osu.notes

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.osu.notes.Note
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(noteManager: NoteManager) {
    val notes by noteManager.notes.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf<Note?>(null) }

    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    var draggedNoteId by remember { mutableStateOf<String?>(null) }
    val hapticFeedback = LocalHapticFeedback.current

    val dragDropState = remember {
        object {
            var draggingItemIndex by mutableStateOf<Int?>(null)
            var draggingItemOffset by mutableStateOf(0f)

            fun onDragStart(index: Int, noteId: String) {
                draggingItemIndex = index
                draggedNoteId = noteId
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }

            fun onDragInterrupted() {
                draggingItemIndex = null
                draggedNoteId = null
                overscrollJob?.cancel()
                overscrollJob = null
                draggingItemOffset = 0f
            }

            fun onDrag(offsetY: Float) {
                draggingItemOffset += offsetY

                val listBounds = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.toFloat() ?: 0f
                val viewPortHeight = lazyListState.layoutInfo.viewportSize.height.toFloat()
                val currentItemCenter = draggingItemOffset + (listBounds / 2f)

                overscrollJob?.cancel()

                if (currentItemCenter < viewPortHeight * 0.2f) {
                    overscrollJob = scope.launch { lazyListState.scrollBy(-20f) }
                } else if (currentItemCenter > viewPortHeight * 0.8f) {
                    overscrollJob = scope.launch { lazyListState.scrollBy(20f) }
                }
            }

            fun onDrop() {
                val currentDraggingItemIndex = draggingItemIndex
                val currentDraggedNoteId = draggedNoteId
                if (currentDraggingItemIndex != null && currentDraggedNoteId != null) {
                    val targetIndex = lazyListState.layoutInfo.visibleItemsInfo
                        .minByOrNull { (draggingItemOffset - it.offset).coerceAtLeast(0f) }
                        ?.index
                        ?: currentDraggingItemIndex

                    val fromNote = notes.find { it.id == currentDraggedNoteId }
                    if (fromNote != null && currentDraggingItemIndex != targetIndex) {
                        noteManager.moveNote(fromNote, targetIndex)
                    }
                }
                onDragInterrupted()
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                itemsIndexed(
                    items = notes,
                    key = { _, note -> note.id }
                ) { index, note ->
                    val isDragging = note.id == draggedNoteId
                    val displacementOffset = if (isDragging) {
                        dragDropState.draggingItemOffset
                    } else {
                        0f
                    }

                    DraggableNoteItem(
                        note = note,
                        isDragging = isDragging,
                        displacementOffset = displacementOffset,
                        onDragStart = { dragDropState.onDragStart(index, note.id) },
                        onDrag = { dragAmount -> dragDropState.onDrag(dragAmount) },
                        onDragEnd = { dragDropState.onDrop() },
                        onDragCancel = { dragDropState.onDragInterrupted() },
                        onClick = { showDetailDialog = note },
                        onDelete = { noteManager.deleteNote(note.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            onNoteAdded = { title, content ->
                noteManager.addNote(
                    Note(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        content = content
                    )
                )
                showAddDialog = false
            }
        )
    }

    showDetailDialog?.let { note ->
        NoteDetailDialog(
            note = note,
            onDismiss = { showDetailDialog = null }
        )
    }
}


@Composable
private fun DraggableNoteItem(
    note: Note,
    isDragging: Boolean,
    displacementOffset: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 2.dp,
        label = "Elevation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isDragging) 0.9f else 1f,
        label = "Alpha"
    )

    Card(
        modifier = Modifier
            .graphicsLayer { translationY = displacementOffset }
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(elevation)
            .alpha(alpha)
            .zIndex(if (isDragging) 1f else 0f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragCancel() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            }
                        )
                    }
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(note.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(note.content, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}