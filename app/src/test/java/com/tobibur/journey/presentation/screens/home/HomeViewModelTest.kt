package com.tobibur.journey.presentation.screens.home

import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.model.StreakStats
import com.tobibur.journey.domain.usecase.DeleteEntryUseCase
import com.tobibur.journey.domain.usecase.GetJournalEntriesUseCase
import com.tobibur.journey.domain.usecase.GetJournalStreakUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var getEntries: GetJournalEntriesUseCase
    private lateinit var deleteEntry: DeleteEntryUseCase
    private lateinit var getStreak: GetJournalStreakUseCase
    private val testDispatcher = StandardTestDispatcher()

    private fun epochMillis(year: Int, month: Int, day: Int): Long =
        ZonedDateTime.of(year, month, day, 12, 0, 0, 0, ZoneId.systemDefault())
            .toInstant().toEpochMilli()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getEntries = mockk()
        deleteEntry = mockk()
        getStreak = mockk()
        every { getEntries() } returns flowOf(emptyList())
        every { getStreak() } returns flowOf(StreakStats(0, 0))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = HomeViewModel(getEntries, deleteEntry, getStreak)

    @Test
    fun `streakStats is updated from the streak use case`() = runTest {
        every { getStreak() } returns flowOf(StreakStats(currentStreak = 4, longestStreak = 9))

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(StreakStats(4, 9), viewModel.streakStats.value)
    }

    @Test
    fun `groupEntriesByMonth groups entries by month sorted descending`() {
        val viewModel = buildViewModel()
        val may = JournalEntry(1, "May", "x", epochMillis(2026, 5, 10))
        val juneA = JournalEntry(2, "JuneA", "x", epochMillis(2026, 6, 1))
        val juneB = JournalEntry(3, "JuneB", "x", epochMillis(2026, 6, 20))

        val grouped = viewModel.groupEntriesByMonth(listOf(may, juneA, juneB))

        assertEquals(2, grouped.size)
        // Most recent month first
        assertEquals(2026, grouped[0].yearMonth.year)
        assertEquals(6, grouped[0].yearMonth.monthValue)
        assertEquals(2, grouped[0].entries.size)
        assertEquals(5, grouped[1].yearMonth.monthValue)
        assertEquals(1, grouped[1].entries.size)
    }

    @Test
    fun `groupEntriesByMonth returns empty list for no entries`() {
        val viewModel = buildViewModel()

        assertEquals(emptyList<Any>(), viewModel.groupEntriesByMonth(emptyList()))
    }

    @Test
    fun `deleteEntry calls use case and invokes callback`() = runTest {
        val entry = JournalEntry(1, "T", "C", 1L)
        coEvery { deleteEntry(entry) } just Runs
        val viewModel = buildViewModel()
        var deleted = false

        viewModel.deleteEntry(entry) { deleted = true }
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteEntry(entry) }
        assertEquals(true, deleted)
    }
}
