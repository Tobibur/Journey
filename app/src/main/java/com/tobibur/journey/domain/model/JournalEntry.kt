package com.tobibur.journey.domain.model

data class JournalEntry(
    val id: Int = 0,
    val title: String = "Untitled",
    val content: String,
    val timestamp: Long
)
