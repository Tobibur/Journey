package com.tobibur.journey.domain.usecase

import android.net.Uri
import com.tobibur.journey.domain.repository.JournalRepository
import com.tobibur.journey.utils.JournalPdfGenerator
import com.tobibur.journey.utils.PdfFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExportJournalToPdfUseCase @Inject constructor(
    private val repository: JournalRepository,
    private val pdfGenerator: JournalPdfGenerator,
    private val pdfFileManager: PdfFileManager
) {

    sealed class Result {
        data class Success(val uri: Uri, val entryCount: Int) : Result()
        data object NoEntries : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(): Result = withContext(Dispatchers.IO) {
        try {
            val entries = repository.getJournalEntries().first()

            if (entries.isEmpty()) {
                return@withContext Result.NoEntries
            }

            val pdfBytes = pdfGenerator.generate(entries)

            val uri = pdfFileManager.savePdfToDownloads(pdfBytes)
                ?: return@withContext Result.Error("Failed to save PDF file")

            Result.Success(uri, entries.size)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error occurred")
        }
    }
}
