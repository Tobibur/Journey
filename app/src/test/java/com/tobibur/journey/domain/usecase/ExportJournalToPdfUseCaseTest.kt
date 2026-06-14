package com.tobibur.journey.domain.usecase

import android.net.Uri
import com.tobibur.journey.data.ExportState
import com.tobibur.journey.data.ExportType
import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.repository.JournalRepository
import com.tobibur.journey.utils.JournalPdfGenerator
import com.tobibur.journey.utils.PdfFileManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportJournalToPdfUseCaseTest {

    private lateinit var repository: JournalRepository
    private lateinit var pdfGenerator: JournalPdfGenerator
    private lateinit var pdfFileManager: PdfFileManager
    private lateinit var useCase: ExportJournalToPdfUseCase

    @Before
    fun setUp() {
        repository = mockk()
        pdfGenerator = mockk()
        pdfFileManager = mockk()
        useCase = ExportJournalToPdfUseCase(repository, pdfGenerator, pdfFileManager)
    }

    @Test
    fun `invoke returns NoEntries when repository has no entries`() = runTest {
        every { repository.getJournalEntries() } returns flowOf(emptyList())

        val result = useCase()

        assertTrue(result is ExportState.NoEntries)
    }

    @Test
    fun `invoke returns Success when entries exist and PDF is saved`() = runTest {
        val entries = listOf(
            JournalEntry(id = 1, title = "Entry 1", content = "Content 1", timestamp = 1000L),
            JournalEntry(id = 2, title = "Entry 2", content = "Content 2", timestamp = 2000L)
        )
        val pdfBytes = byteArrayOf(1, 2, 3)
        val mockUri = mockk<Uri>()

        every { repository.getJournalEntries() } returns flowOf(entries)
        every { pdfGenerator.generate(entries) } returns pdfBytes
        every { pdfFileManager.savePdfToDownloads(pdfBytes) } returns mockUri

        val result = useCase()

        assertTrue(result is ExportState.Success)
        val success = result as ExportState.Success
        assertEquals(mockUri, success.uri)
        assertEquals(2, success.entryCount)
        assertEquals(ExportType.PDF, success.exportType)
    }

    @Test
    fun `invoke returns Error when savePdfToDownloads returns null`() = runTest {
        val entries = listOf(
            JournalEntry(id = 1, title = "Entry 1", content = "Content 1", timestamp = 1000L)
        )
        val pdfBytes = byteArrayOf(1, 2, 3)

        every { repository.getJournalEntries() } returns flowOf(entries)
        every { pdfGenerator.generate(entries) } returns pdfBytes
        every { pdfFileManager.savePdfToDownloads(pdfBytes) } returns null

        val result = useCase()

        assertTrue(result is ExportState.Error)
        assertEquals("Failed to save PDF file", (result as ExportState.Error).message)
    }

    @Test
    fun `invoke returns Error with exception message when exception is thrown`() = runTest {
        val errorMessage = "Database error"
        every { repository.getJournalEntries() } throws RuntimeException(errorMessage)

        val result = useCase()

        assertTrue(result is ExportState.Error)
        assertEquals(errorMessage, (result as ExportState.Error).message)
    }

    @Test
    fun `invoke returns Error with unknown message when exception has no message`() = runTest {
        every { repository.getJournalEntries() } throws RuntimeException()

        val result = useCase()

        assertTrue(result is ExportState.Error)
        assertEquals("Unknown error occurred", (result as ExportState.Error).message)
    }

    @Test
    fun `invoke calls pdfGenerator generate with entries from repository`() = runTest {
        val entries = listOf(
            JournalEntry(id = 1, title = "Test", content = "Content", timestamp = 1000L)
        )
        val pdfBytes = byteArrayOf(1, 2, 3)
        val mockUri = mockk<Uri>()

        every { repository.getJournalEntries() } returns flowOf(entries)
        every { pdfGenerator.generate(entries) } returns pdfBytes
        every { pdfFileManager.savePdfToDownloads(pdfBytes) } returns mockUri

        useCase()

        verify { pdfGenerator.generate(entries) }
    }

    @Test
    fun `invoke calls pdfFileManager with generated PDF bytes`() = runTest {
        val entries = listOf(
            JournalEntry(id = 1, title = "Test", content = "Content", timestamp = 1000L)
        )
        val pdfBytes = byteArrayOf(1, 2, 3, 4, 5)
        val mockUri = mockk<Uri>()

        every { repository.getJournalEntries() } returns flowOf(entries)
        every { pdfGenerator.generate(entries) } returns pdfBytes
        every { pdfFileManager.savePdfToDownloads(pdfBytes) } returns mockUri

        useCase()

        verify { pdfFileManager.savePdfToDownloads(pdfBytes) }
    }
}
