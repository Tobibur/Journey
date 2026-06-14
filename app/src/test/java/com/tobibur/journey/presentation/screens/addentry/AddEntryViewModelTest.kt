package com.tobibur.journey.presentation.screens.addentry

import app.cash.turbine.test
import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.model.StreakStats
import com.tobibur.journey.domain.usecase.AddEntryUseCase
import com.tobibur.journey.domain.usecase.GetEntryByIdUseCase
import com.tobibur.journey.domain.usecase.GetJournalStreakUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
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

@OptIn(ExperimentalCoroutinesApi::class)
class AddEntryViewModelTest {

    private lateinit var addEntry: AddEntryUseCase
    private lateinit var getEntryById: GetEntryByIdUseCase
    private lateinit var getStreak: GetJournalStreakUseCase
    private lateinit var viewModel: AddEntryViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        addEntry = mockk()
        getEntryById = mockk()
        getStreak = mockk()
        every { getStreak() } returns flowOf(StreakStats(0, 0))
        viewModel = AddEntryViewModel(addEntry, getEntryById, getStreak)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onTitleChange and onContentChange update state`() {
        viewModel.onTitleChange("Hello")
        viewModel.onContentChange("World")

        assertEquals("Hello", viewModel.title.value)
        assertEquals("World", viewModel.content.value)
    }

    @Test
    fun `saveEntry does nothing when title and content are blank`() = runTest {
        var saved = false

        viewModel.saveEntry { saved = true }
        advanceUntilIdle()

        coVerify(exactly = 0) { addEntry(any()) }
        assertEquals(false, saved)
    }

    @Test
    fun `saveEntry persists entry and calls onSaved when streak does not increase`() = runTest {
        coEvery { addEntry(any()) } just Runs
        every { getStreak() } returns flowOf(StreakStats(1, 1))
        viewModel.onContentChange("Some content")
        var saved = false

        viewModel.saveEntry { saved = true }
        advanceUntilIdle()

        coVerify(exactly = 1) { addEntry(any()) }
        assertEquals(true, saved)
    }

    @Test
    fun `saveEntry emits streak popup and does not call onSaved when streak increases`() = runTest {
        coEvery { addEntry(any()) } just Runs
        // streak before = 1, streak after = 2
        every { getStreak() } returnsMany listOf(
            flowOf(StreakStats(1, 1)),
            flowOf(StreakStats(2, 2))
        )
        viewModel.onContentChange("Today was good")
        var saved = false

        viewModel.showStreakPopup.test {
            viewModel.saveEntry { saved = true }
            advanceUntilIdle()

            assertEquals(2, awaitItem())
        }
        assertEquals(false, saved)
    }

    @Test
    fun `saveEntry on a new entry uses id zero`() = runTest {
        val captured = slot<JournalEntry>()
        coEvery { addEntry(capture(captured)) } just Runs
        every { getStreak() } returns flowOf(StreakStats(1, 1))
        viewModel.onTitleChange("New")

        viewModel.saveEntry { }
        advanceUntilIdle()

        assertEquals(0, captured.captured.id)
        assertEquals("New", captured.captured.title)
    }

    @Test
    fun `loadEntry populates fields and saveEntry reuses the loaded id`() = runTest {
        coEvery { getEntryById(5) } returns JournalEntry(5, "Old", "Body", 1234L)
        val captured = slot<JournalEntry>()
        coEvery { addEntry(capture(captured)) } just Runs
        every { getStreak() } returns flowOf(StreakStats(1, 1))

        viewModel.loadEntry(5)
        advanceUntilIdle()

        assertEquals("Old", viewModel.title.value)
        assertEquals("Body", viewModel.content.value)
        assertEquals(1234L, viewModel.timestamp.value)

        viewModel.saveEntry { }
        advanceUntilIdle()

        assertEquals(5, captured.captured.id)
    }
}
