package com.tobibur.journey.domain.usecase

import com.tobibur.journey.data.ExportState
import com.tobibur.journey.data.ExportType
import com.tobibur.journey.domain.repository.JournalRepository
import com.tobibur.journey.utils.JsonFileManager
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ExportJournalToJsonUseCase @Inject constructor(
    private val repository: JournalRepository,
    private val jsonFileManager: JsonFileManager
) {
    suspend operator fun invoke(): ExportState = withContext(Dispatchers.IO) {
        try {
            val entries = repository.getJournalEntries().first()

            if (entries.isEmpty()) {
                return@withContext ExportState.NoEntries
            }

            val jsonBytes = jsonFileManager.generate(entries)

            val uri = jsonFileManager.saveJsonToDownloads(jsonBytes)
                ?: return@withContext ExportState.Error("Failed to save JSON file")

            ExportState.Success(uri, entries.size, ExportType.JSON)
        } catch (e: Exception) {
            ExportState.Error(e.message ?: "Unknown error occurred")
        }
    }

}