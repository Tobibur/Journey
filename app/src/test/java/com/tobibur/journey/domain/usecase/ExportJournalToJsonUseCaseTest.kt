package com.tobibur.journey.domain.usecase

import android.net.Uri
import com.tobibur.journey.data.ExportState
import com.tobibur.journey.data.ExportType
import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.repository.JournalRepository
import com.tobibur.journey.utils.JsonFileManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportJournalToJsonUseCaseTest {

    private lateinit var repository: JournalRepository
    private lateinit var jsonFileManager: JsonFileManager
    private lateinit var useCase: ExportJournalToJsonUseCase

    private val entries = listOf(JournalEntry(1, "A", "a", 1L))
    private val jsonBytes = byteArrayOf(1, 2, 3)

    @Before
    fun setUp() {
        repository = mockk()
        jsonFileManager = mockk()
        useCase = ExportJournalToJsonUseCase(repository, jsonFileManager)
    }

    @Test
    fun `returns NoEntries when repository is empty`() = runTest {
        every { repository.getJournalEntries() } returns flowOf(emptyList())

        assertTrue(useCase() is ExportState.NoEntries)
    }

    @Test
    fun `returns Success with uri count and JSON type`() = runTest {
        val uri = mockk<Uri>()
        every { repository.getJournalEntries() } returns flowOf(entries)
        every { jsonFileManager.generate(entries) } returns jsonBytes
        every { jsonFileManager.saveJsonToDownloads(jsonBytes) } returns uri

        val result = useCase()

        assertTrue(result is ExportState.Success)
        result as ExportState.Success
        assertEquals(uri, result.uri)
        assertEquals(1, result.entryCount)
        assertEquals(ExportType.JSON, result.exportType)
    }

    @Test
    fun `returns Error when saving fails`() = runTest {
        every { repository.getJournalEntries() } returns flowOf(entries)
        every { jsonFileManager.generate(entries) } returns jsonBytes
        every { jsonFileManager.saveJsonToDownloads(jsonBytes) } returns null

        val result = useCase()

        assertTrue(result is ExportState.Error)
        assertEquals("Failed to save JSON file", (result as ExportState.Error).message)
    }

    @Test
    fun `returns Error with exception message on failure`() = runTest {
        every { repository.getJournalEntries() } throws RuntimeException("boom")

        val result = useCase()

        assertEquals("boom", (result as ExportState.Error).message)
    }
}
