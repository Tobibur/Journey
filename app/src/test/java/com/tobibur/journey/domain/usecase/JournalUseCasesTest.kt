package com.tobibur.journey.domain.usecase

import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.repository.JournalRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Covers the thin single-responsibility use cases that delegate to the repository.
 */
class JournalUseCasesTest {

    private lateinit var repository: JournalRepository

    private val sampleEntry = JournalEntry(id = 1, title = "T", content = "C", timestamp = 10L)

    @Before
    fun setUp() {
        repository = mockk()
    }

    @Test
    fun `AddEntryUseCase delegates to repository`() = runTest {
        coEvery { repository.addJournalEntry(sampleEntry) } just Runs

        AddEntryUseCase(repository).invoke(sampleEntry)

        coVerify(exactly = 1) { repository.addJournalEntry(sampleEntry) }
    }

    @Test
    fun `DeleteEntryUseCase delegates to repository`() = runTest {
        coEvery { repository.deleteJournalEntry(sampleEntry) } just Runs

        DeleteEntryUseCase(repository).invoke(sampleEntry)

        coVerify(exactly = 1) { repository.deleteJournalEntry(sampleEntry) }
    }

    @Test
    fun `DeleteAllEntriesUseCase returns deleted count`() = runTest {
        coEvery { repository.deleteAllJournalEntries() } returns 7

        val count = DeleteAllEntriesUseCase(repository).invoke()

        assertEquals(7, count)
        coVerify(exactly = 1) { repository.deleteAllJournalEntries() }
    }

    @Test
    fun `GetEntryByIdUseCase returns entry from repository`() = runTest {
        coEvery { repository.getJournalEntryById(1) } returns sampleEntry

        assertEquals(sampleEntry, GetEntryByIdUseCase(repository).invoke(1))
    }

    @Test
    fun `GetEntryByIdUseCase returns null when not found`() = runTest {
        coEvery { repository.getJournalEntryById(404) } returns null

        assertNull(GetEntryByIdUseCase(repository).invoke(404))
    }

    @Test
    fun `GetJournalEntriesUseCase emits repository entries`() = runTest {
        val entries = listOf(sampleEntry)
        every { repository.getJournalEntries() } returns flowOf(entries)

        val result = GetJournalEntriesUseCase(repository).invoke().first()

        assertEquals(entries, result)
        verify { repository.getJournalEntries() }
    }
}
