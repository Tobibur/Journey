package com.tobibur.journey.domain.usecase

import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow

class GetJournalEntriesUseCase(
    private val repository: JournalRepository
) {
    operator fun invoke(): Flow<List<JournalEntry>> {
        return repository.getJournalEntries()
    }
}