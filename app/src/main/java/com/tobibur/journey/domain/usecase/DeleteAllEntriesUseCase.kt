package com.tobibur.journey.domain.usecase

import com.tobibur.journey.domain.repository.JournalRepository

class DeleteAllEntriesUseCase(
    private val repository: JournalRepository
){
    suspend operator fun invoke(): Int{
       return repository.deleteAllJournalEntries()
    }
}