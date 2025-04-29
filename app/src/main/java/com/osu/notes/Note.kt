package com.osu.notes

import com.android.identity.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = createdAt
)
