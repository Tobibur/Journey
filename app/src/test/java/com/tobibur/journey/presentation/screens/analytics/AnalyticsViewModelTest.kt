package com.tobibur.journey.presentation.screens.analytics

import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.model.StreakStats
import com.tobibur.journey.domain.usecase.GetJournalEntriesUseCase
import com.tobibur.journey.domain.usecase.GetJournalStreakUseCase
import io.mockk.every
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
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    private lateinit var getEntries: GetJournalEntriesUseCase
    private lateinit var getStreak: GetJournalStreakUseCase
    private val testDispatcher = StandardTestDispatcher()

    private fun millisAtNoon(date: LocalDate): Long =
        date.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getEntries = mockk()
        getStreak = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default ui state is empty before data arrives`() {
        every { getEntries() } returns flowOf(emptyList())
        every { getStreak() } returns flowOf(StreakStats(0, 0))

        val viewModel = AnalyticsViewModel(getEntries, getStreak)

        // before the dispatcher runs the init coroutine
        assertEquals(AnalyticsUiState(), viewModel.uiState.value)
    }

    @Test
    fun `ui state aggregates streak total and today counts`() = runTest {
        val today = LocalDate.now()
        val entries = listOf(
            JournalEntry(1, "a", "a", millisAtNoon(today)),
            JournalEntry(2, "b", "b", millisAtNoon(today)),
            JournalEntry(3, "c", "c", millisAtNoon(today.minusMonths(2)))
        )
        every { getEntries() } returns flowOf(entries)
        every { getStreak() } returns flowOf(StreakStats(currentStreak = 3, longestStreak = 8))

        val viewModel = AnalyticsViewModel(getEntries, getStreak)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.currentStreak)
        assertEquals(8, state.highestStreak)
        assertEquals(3, state.totalEntries)
        assertEquals(2, state.entriesToday)
        assertEquals(2, state.entriesByDate[today])
    }

    @Test
    fun `doneDatesThisMonth excludes entries from other months`() = runTest {
        val today = LocalDate.now()
        val lastMonth = today.minusMonths(1)
        val entries = listOf(
            JournalEntry(1, "a", "a", millisAtNoon(today)),
            JournalEntry(2, "b", "b", millisAtNoon(lastMonth))
        )
        every { getEntries() } returns flowOf(entries)
        every { getStreak() } returns flowOf(StreakStats(1, 1))

        val viewModel = AnalyticsViewModel(getEntries, getStreak)
        advanceUntilIdle()

        val done = viewModel.uiState.value.doneDatesThisMonth
        assertEquals(listOf(today), done)
    }

    @Test
    fun `empty data produces zeroed ui state`() = runTest {
        every { getEntries() } returns flowOf(emptyList())
        every { getStreak() } returns flowOf(StreakStats(0, 0))

        val viewModel = AnalyticsViewModel(getEntries, getStreak)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.totalEntries)
        assertEquals(0, state.entriesToday)
        assertEquals(emptyList<LocalDate>(), state.doneDatesThisMonth)
    }
}
