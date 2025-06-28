package com.tobibur.journey.domain.repository

import com.tobibur.journey.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun getJournalEntries(): Flow<List<JournalEntry>>
    suspend fun addJournalEntry(entry: JournalEntry)
    suspend fun deleteJournalEntry(entry: JournalEntry)
    suspend fun getJournalEntryById(id: Int): JournalEntry?
}