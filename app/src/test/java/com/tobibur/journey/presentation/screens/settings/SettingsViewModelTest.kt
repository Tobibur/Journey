package com.tobibur.journey.presentation.screens.settings

import android.net.Uri
import app.cash.turbine.test
import com.tobibur.journey.data.ExportState
import com.tobibur.journey.data.ExportType
import com.tobibur.journey.data.ImportState
import com.tobibur.journey.data.local.datastore.SettingsPreferences
import com.tobibur.journey.domain.usecase.DeleteAllEntriesUseCase
import com.tobibur.journey.domain.usecase.ExportJournalToJsonUseCase
import com.tobibur.journey.domain.usecase.ExportJournalToPdfUseCase
import com.tobibur.journey.domain.usecase.ImportJournalFromJsonUseCase
import com.tobibur.journey.notifications.ReminderScheduler
import com.tobibur.journey.ui.theme.AppThemeType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var prefs: SettingsPreferences
    private lateinit var exportPdfUseCase: ExportJournalToPdfUseCase
    private lateinit var exportJsonUseCase: ExportJournalToJsonUseCase
    private lateinit var importJsonUseCase: ImportJournalFromJsonUseCase
    private lateinit var deleteAllUseCase: DeleteAllEntriesUseCase
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        prefs = mockk(relaxed = true)
        exportPdfUseCase = mockk()
        exportJsonUseCase = mockk()
        importJsonUseCase = mockk()
        deleteAllUseCase = mockk()
        reminderScheduler = mockk(relaxed = true)

        every { prefs.appThemeTypeFlow } returns flowOf(AppThemeType.PINK)
        every { prefs.useDynamicColorFlow } returns flowOf(true)
        every { prefs.darkThemeFlow } returns flowOf(false)
        every { prefs.appLockEnabledFlow } returns flowOf(false)
        every { prefs.reminderEnabledFlow } returns flowOf(false)
        every { prefs.reminderTimeFlow } returns flowOf(20 * 60)

        viewModel = SettingsViewModel(
            prefs,
            exportPdfUseCase,
            exportJsonUseCase,
            importJsonUseCase,
            deleteAllUseCase,
            reminderScheduler
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exportState initial value is Idle`() {
        assertEquals(ExportUiState.Idle, viewModel.exportState.value)
    }

    @Test
    fun `importState initial value is Idle`() {
        assertEquals(ImportState.Idle, viewModel.importState.value)
    }

    // ---- PDF export ----

    @Test
    fun `exportPDFJournal sets Loading then Success`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { exportPdfUseCase() } returns ExportState.Success(mockUri, 5, ExportType.PDF)

        viewModel.exportState.test {
            assertEquals(ExportUiState.Idle, awaitItem())

            viewModel.exportPDFJournal()
            advanceUntilIdle()

            assertEquals(ExportUiState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is ExportUiState.Success)
            success as ExportUiState.Success
            assertEquals(mockUri, success.uri)
            assertEquals(5, success.entryCount)
            assertEquals(ExportType.PDF, success.type)
        }
    }

    @Test
    fun `exportPDFJournal sets NoEntries when there are none`() = runTest {
        coEvery { exportPdfUseCase() } returns ExportState.NoEntries

        viewModel.exportState.test {
            assertEquals(ExportUiState.Idle, awaitItem())

            viewModel.exportPDFJournal()
            advanceUntilIdle()

            assertEquals(ExportUiState.Loading, awaitItem())
            assertEquals(ExportUiState.NoEntries, awaitItem())
        }
    }

    @Test
    fun `exportPDFJournal sets Error on failure`() = runTest {
        coEvery { exportPdfUseCase() } returns ExportState.Error("boom")

        viewModel.exportState.test {
            assertEquals(ExportUiState.Idle, awaitItem())

            viewModel.exportPDFJournal()
            advanceUntilIdle()

            assertEquals(ExportUiState.Loading, awaitItem())
            assertEquals(ExportUiState.Error("boom"), awaitItem())
        }
    }

    // ---- JSON export ----

    @Test
    fun `exportJsonJournal sets Success with JSON type`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { exportJsonUseCase() } returns ExportState.Success(mockUri, 3, ExportType.JSON)

        viewModel.exportState.test {
            assertEquals(ExportUiState.Idle, awaitItem())

            viewModel.exportJsonJournal()
            advanceUntilIdle()

            assertEquals(ExportUiState.Loading, awaitItem())
            val success = awaitItem() as ExportUiState.Success
            assertEquals(ExportType.JSON, success.type)
            assertEquals(3, success.entryCount)
        }
    }

    @Test
    fun `resetExportState sets state back to Idle`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { exportPdfUseCase() } returns ExportState.Success(mockUri, 3, ExportType.PDF)

        viewModel.exportState.test {
            assertEquals(ExportUiState.Idle, awaitItem())

            viewModel.exportPDFJournal()
            advanceUntilIdle()
            skipItems(2)

            viewModel.resetExportState()
            assertEquals(ExportUiState.Idle, awaitItem())
        }
    }

    // ---- JSON import ----

    @Test
    fun `importFromJson with null bytes sets Error`() = runTest {
        viewModel.importState.test {
            assertEquals(ImportState.Idle, awaitItem())

            viewModel.importFromJson(null)
            advanceUntilIdle()

            assertEquals(ImportState.Loading, awaitItem())
            assertEquals(ImportState.Error("No file selected"), awaitItem())
        }
    }

    @Test
    fun `importFromJson forwards use case result`() = runTest {
        val bytes = byteArrayOf(1, 2, 3)
        coEvery { importJsonUseCase(bytes) } returns ImportState.Success(4)

        viewModel.importState.test {
            assertEquals(ImportState.Idle, awaitItem())

            viewModel.importFromJson(bytes)
            advanceUntilIdle()

            assertEquals(ImportState.Loading, awaitItem())
            assertEquals(ImportState.Success(4), awaitItem())
        }
    }

    @Test
    fun `resetImportState sets state back to Idle`() = runTest {
        viewModel.importFromJson(null)
        advanceUntilIdle()

        viewModel.resetImportState()

        assertEquals(ImportState.Idle, viewModel.importState.value)
    }

    // ---- delete all ----

    @Test
    fun `deleteAllEntries returns deleted count from use case`() = runTest {
        coEvery { deleteAllUseCase() } returns 12

        val deleted = viewModel.deleteAllEntries()

        assertEquals(12, deleted)
        coVerify(exactly = 1) { deleteAllUseCase() }
    }

    // ---- preferences ----

    @Test
    fun `setAccentColor calls prefs setAppThemeType`() = runTest {
        viewModel.setAccentColor("PURPLE")
        advanceUntilIdle()

        coVerify { prefs.setAppThemeType(AppThemeType.PURPLE) }
    }

    @Test
    fun `setDynamicColor calls prefs setUseDynamicColor`() = runTest {
        viewModel.setDynamicColor(false)
        advanceUntilIdle()

        coVerify { prefs.setUseDynamicColor(false) }
    }

    @Test
    fun `setDarkTheme calls prefs setDarkTheme`() = runTest {
        viewModel.setDarkTheme(true)
        advanceUntilIdle()

        coVerify { prefs.setDarkTheme(true) }
    }

    @Test
    fun `setAppLockEnabled calls prefs setAppLockEnabled`() = runTest {
        viewModel.setAppLockEnabled(true)
        advanceUntilIdle()

        coVerify { prefs.setAppLockEnabled(true) }
    }

    // ---- reminders ----

    @Test
    fun `setReminderEnabled true persists and schedules at current time`() = runTest {
        viewModel.setReminderEnabled(true)
        advanceUntilIdle()

        coVerify { prefs.setReminderEnabled(true) }
        // Default reminderTime is 20 * 60 = 8:00 PM.
        verify { reminderScheduler.schedule(20, 0) }
    }

    @Test
    fun `setReminderEnabled false persists and cancels schedule`() = runTest {
        viewModel.setReminderEnabled(false)
        advanceUntilIdle()

        coVerify { prefs.setReminderEnabled(false) }
        verify { reminderScheduler.cancel() }
    }

    @Test
    fun `setReminderTime persists the time`() = runTest {
        viewModel.setReminderTime(7, 30)
        advanceUntilIdle()

        coVerify { prefs.setReminderTime(7, 30) }
    }

    @Test
    fun `setReminderTime does not reschedule when reminder disabled`() = runTest {
        viewModel.setReminderTime(7, 30)
        advanceUntilIdle()

        verify(exactly = 0) { reminderScheduler.schedule(any(), any()) }
    }
}
