package com.tobibur.journey.domain.usecase

import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.repository.JournalRepository

class AddEntryUseCase(
    private val repository: JournalRepository
) {
    suspend operator fun invoke(entry: JournalEntry) {
        repository.addJournalEntry(entry)
    }
}