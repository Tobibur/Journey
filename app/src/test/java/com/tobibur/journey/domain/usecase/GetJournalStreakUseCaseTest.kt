package com.tobibur.journey.domain.usecase

import com.tobibur.journey.domain.repository.JournalRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetJournalStreakUseCaseTest {

    private lateinit var repository: JournalRepository
    private lateinit var useCase: GetJournalStreakUseCase

    private val today: LocalDate = LocalDate.now()

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetJournalStreakUseCase(repository)
    }

    @Test
    fun `empty dates returns zero streaks`() = runTest {
        every { repository.getAllEntryDatesFlow() } returns flowOf(emptyList())

        val stats = useCase().first()

        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.longestStreak)
    }

    @Test
    fun `single entry today gives current streak of one`() = runTest {
        every { repository.getAllEntryDatesFlow() } returns flowOf(listOf(today))

        val stats = useCase().first()

        assertEquals(1, stats.currentStreak)
        assertEquals(1, stats.longestStreak)
    }

    @Test
    fun `three consecutive days ending today gives streak of three`() = runTest {
        val dates = listOf(today, today.minusDays(1), today.minusDays(2))
        every { repository.getAllEntryDatesFlow() } returns flowOf(dates)

        val stats = useCase().first()

        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.longestStreak)
    }

    @Test
    fun `streak ending yesterday with no entry today is still counted`() = runTest {
        val dates = listOf(today.minusDays(1), today.minusDays(2))
        every { repository.getAllEntryDatesFlow() } returns flowOf(dates)

        val stats = useCase().first()

        assertEquals(2, stats.currentStreak)
        assertEquals(2, stats.longestStreak)
    }

    @Test
    fun `streak that ended before yesterday gives current streak of zero`() = runTest {
        val dates = listOf(today.minusDays(3), today.minusDays(4))
        every { repository.getAllEntryDatesFlow() } returns flowOf(dates)

        val stats = useCase().first()

        assertEquals(0, stats.currentStreak)
        // longest still reflects the consecutive leading run
        assertEquals(2, stats.longestStreak)
    }

    @Test
    fun `gap between today and older entry breaks current streak at one`() = runTest {
        val dates = listOf(today, today.minusDays(5))
        every { repository.getAllEntryDatesFlow() } returns flowOf(dates)

        val stats = useCase().first()

        assertEquals(1, stats.currentStreak)
        assertEquals(1, stats.longestStreak)
    }

    @Test
    fun `duplicate dates are de-duplicated before counting`() = runTest {
        val dates = listOf(today, today, today.minusDays(1), today.minusDays(1))
        every { repository.getAllEntryDatesFlow() } returns flowOf(dates)

        val stats = useCase().first()

        assertEquals(2, stats.currentStreak)
        assertEquals(2, stats.longestStreak)
    }

    @Test
    fun `unsorted input is handled correctly`() = runTest {
        val dates = listOf(today.minusDays(2), today, today.minusDays(1))
        every { repository.getAllEntryDatesFlow() } returns flowOf(dates)

        val stats = useCase().first()

        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.longestStreak)
    }
}
