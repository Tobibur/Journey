package com.tobibur.journey.presentation.screens.viewentry

import com.tobibur.journey.domain.model.JournalEntry
import com.tobibur.journey.domain.usecase.DeleteEntryUseCase
import com.tobibur.journey.domain.usecase.GetEntryByIdUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewEntryViewModelTest {

    private lateinit var getEntryById: GetEntryByIdUseCase
    private lateinit var deleteEntry: DeleteEntryUseCase
    private lateinit var viewModel: ViewEntryViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getEntryById = mockk()
        deleteEntry = mockk()
        viewModel = ViewEntryViewModel(getEntryById, deleteEntry)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadEntry populates entry and timestamp`() = runTest {
        val entry = JournalEntry(1, "Title", "Body", 5000L)
        coEvery { getEntryById(1) } returns entry

        viewModel.loadEntry(1)
        advanceUntilIdle()

        assertEquals(entry, viewModel.entry.value)
        assertEquals(5000L, viewModel.timestamp.value)
    }

    @Test
    fun `loadEntry leaves entry null when not found`() = runTest {
        coEvery { getEntryById(2) } returns null

        viewModel.loadEntry(2)
        advanceUntilIdle()

        assertNull(viewModel.entry.value)
    }

    @Test
    fun `deleteEntry deletes loaded entry and invokes callback`() = runTest {
        val entry = JournalEntry(1, "Title", "Body", 5000L)
        coEvery { getEntryById(1) } returns entry
        coEvery { deleteEntry(entry) } just Runs
        viewModel.loadEntry(1)
        advanceUntilIdle()
        var deleted = false

        viewModel.deleteEntry { deleted = true }
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteEntry(entry) }
        assertEquals(true, deleted)
    }

    @Test
    fun `deleteEntry does nothing when no entry loaded`() = runTest {
        var deleted = false

        viewModel.deleteEntry { deleted = true }
        advanceUntilIdle()

        coVerify(exactly = 0) { deleteEntry(any()) }
        assertEquals(false, deleted)
    }
}
