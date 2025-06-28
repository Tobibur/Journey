package com.tobibur.journey.domain.usecase

import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.repository.JournalRepository

class GetEntryByIdUseCase(
    private val repository: JournalRepository
) {
    suspend operator fun invoke(id: Int): JournalEntry? {
        return repository.getJournalEntryById(id)
    }
}
