package com.tobibur.journey.data.repository

import com.tobibur.journey.data.local.dao.JournalDao
import com.tobibur.journey.data.local.entity.JournalEntity
import com.tobibur.journey.domain.model.JournalEntry
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class JournalRepositoryImplTest {

    private lateinit var dao: JournalDao
    private lateinit var repository: JournalRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        repository = JournalRepositoryImpl(dao)
    }

    @Test
    fun `getJournalEntries maps entities to domain models`() = runTest {
        val entities = listOf(
            JournalEntity(id = 1, title = "A", content = "a", timestamp = 100L),
            JournalEntity(id = 2, title = "B", content = "b", timestamp = 200L)
        )
        coEvery { dao.getAllEntries() } returns flowOf(entities)

        val result = repository.getJournalEntries().first()

        assertEquals(2, result.size)
        assertEquals(JournalEntry(1, "A", "a", 100L), result[0])
        assertEquals(JournalEntry(2, "B", "b", 200L), result[1])
    }

    @Test
    fun `addJournalEntry converts domain to entity and inserts`() = runTest {
        val captured = slot<JournalEntity>()
        coEvery { dao.insertEntry(capture(captured)) } just Runs

        repository.addJournalEntry(JournalEntry(id = 5, title = "X", content = "y", timestamp = 1L))

        coVerify { dao.insertEntry(any()) }
        assertEquals(5, captured.captured.id)
        assertEquals("X", captured.captured.title)
    }

    @Test
    fun `deleteJournalEntry delegates to dao`() = runTest {
        coEvery { dao.deleteEntry(any()) } just Runs

        repository.deleteJournalEntry(JournalEntry(id = 1, title = "X", content = "y", timestamp = 1L))

        coVerify { dao.deleteEntry(any()) }
    }

    @Test
    fun `deleteAllJournalEntries returns deleted count from dao`() = runTest {
        coEvery { dao.deleteAllEntries() } returns 4

        assertEquals(4, repository.deleteAllJournalEntries())
    }

    @Test
    fun `getJournalEntryById maps entity to domain`() = runTest {
        coEvery { dao.getEntryById(1) } returns JournalEntity(1, "T", "C", 50L)

        val result = repository.getJournalEntryById(1)

        assertEquals(JournalEntry(1, "T", "C", 50L), result)
    }

    @Test
    fun `getJournalEntryById returns null when dao returns null`() = runTest {
        coEvery { dao.getEntryById(99) } returns null

        assertNull(repository.getJournalEntryById(99))
    }

    @Test
    fun `getAllEntryDatesFlow parses date strings into LocalDate`() = runTest {
        coEvery { dao.getAllEntryDatesFlow() } returns flowOf(listOf("2026-06-14", "2026-06-13"))

        val result = repository.getAllEntryDatesFlow().first()

        assertEquals(listOf(LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 13)), result)
    }

    @Test
    fun `getAllEntryDatesFlow returns empty list when dao emits empty`() = runTest {
        coEvery { dao.getAllEntryDatesFlow() } returns flowOf(emptyList())

        assertEquals(emptyList<LocalDate>(), repository.getAllEntryDatesFlow().first())
    }
}
