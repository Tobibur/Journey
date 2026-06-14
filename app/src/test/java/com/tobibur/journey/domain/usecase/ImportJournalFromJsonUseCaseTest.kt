package com.tobibur.journey.domain.usecase

import com.tobibur.journey.data.ImportState
import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.repository.JournalRepository
import com.tobibur.journey.utils.JsonFileManager
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImportJournalFromJsonUseCaseTest {

    private lateinit var repository: JournalRepository
    private lateinit var jsonFileManager: JsonFileManager
    private lateinit var useCase: ImportJournalFromJsonUseCase

    private val bytes = "{}".toByteArray()

    @Before
    fun setUp() {
        repository = mockk()
        jsonFileManager = mockk()
        useCase = ImportJournalFromJsonUseCase(repository, jsonFileManager)
    }

    @Test
    fun `returns NoEntries when parsed list is empty`() = runTest {
        every { jsonFileManager.parse(bytes) } returns emptyList()

        val result = useCase(bytes)

        assertTrue(result is ImportState.NoEntries)
        coVerify(exactly = 0) { repository.addJournalEntry(any()) }
    }

    @Test
    fun `returns Success with count and adds every entry`() = runTest {
        val entries = listOf(
            JournalEntry(0, "A", "a", 1L),
            JournalEntry(0, "B", "b", 2L)
        )
        every { jsonFileManager.parse(bytes) } returns entries
        coEvery { repository.addJournalEntry(any()) } just Runs

        val result = useCase(bytes)

        assertEquals(ImportState.Success(2), result)
        coVerify(exactly = 1) { repository.addJournalEntry(entries[0]) }
        coVerify(exactly = 1) { repository.addJournalEntry(entries[1]) }
    }

    @Test
    fun `returns Error with exception message on failure`() = runTest {
        every { jsonFileManager.parse(bytes) } throws RuntimeException("bad json")

        val result = useCase(bytes)

        assertTrue(result is ImportState.Error)
        assertEquals("bad json", (result as ImportState.Error).message)
    }

    @Test
    fun `returns Error with fallback message when exception has no message`() = runTest {
        every { jsonFileManager.parse(bytes) } throws RuntimeException()

        val result = useCase(bytes)

        assertEquals("Unknown error occurred", (result as ImportState.Error).message)
    }
}
