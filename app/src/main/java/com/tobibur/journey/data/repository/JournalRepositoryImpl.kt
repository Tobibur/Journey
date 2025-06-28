package com.tobibur.journey.data.repository

import com.tobibur.journey.data.local.dao.JournalDao
import com.tobibur.journey.data.local.entity.toDomain
import com.tobibur.journey.data.local.entity.toEntity
import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JournalRepositoryImpl(
    private val dao: JournalDao
) : JournalRepository {

    override fun getJournalEntries(): Flow<List<JournalEntry>> {
        return dao.getAllEntries().map { it.map { it.toDomain() } }
    }

    override suspend fun addJournalEntry(entry: JournalEntry) {
        dao.insertEntry(entry.toEntity())
    }

    override suspend fun deleteJournalEntry(entry: JournalEntry) {
        dao.deleteEntry(entry.toEntity())
    }

    override suspend fun getJournalEntryById(id: Int): JournalEntry? {
        return dao.getEntryById(id)?.toDomain()
    }

}
