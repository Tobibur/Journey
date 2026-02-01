package com.tobibur.journey.domain.usecase

import android.net.Uri
import android.util.Log.e
import com.tobibur.journey.data.ImportState
import com.tobibur.journey.domain.repository.JournalRepository
import com.tobibur.journey.utils.JsonFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImportJournalFromJsonUseCase @Inject constructor(
    private val repository: JournalRepository,
    private val jsonFileManager: JsonFileManager
) {

    suspend operator fun invoke(jsonBytes: ByteArray): ImportState = withContext(Dispatchers.IO) {
        try {
            val entries = jsonFileManager.parse(jsonBytes)
            if(entries.isEmpty())
                return@withContext ImportState.NoEntries

            entries.forEach { repository.addJournalEntry(it) }

            ImportState.Success(entries.size)
        }catch (e: Exception){
            ImportState.Error(e.message ?: "Unknown error occurred")
        }
    }


}