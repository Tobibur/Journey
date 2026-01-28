package com.tobibur.journey.presentation.screens.settings

import android.net.Uri
import app.cash.turbine.test
import com.tobibur.journey.data.local.datastore.SettingsPreferences
import com.tobibur.journey.domain.usecase.ExportJournalToPdfUseCase
import com.tobibur.journey.ui.theme.AppThemeType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
    private lateinit var exportUseCase: ExportJournalToPdfUseCase
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        prefs = mockk(relaxed = true)
        exportUseCase = mockk()

        every { prefs.appThemeTypeFlow } returns flowOf(AppThemeType.PINK)
        every { prefs.useDynamicColorFlow } returns flowOf(true)
        every { prefs.darkThemeFlow } returns flowOf(false)
        every { prefs.appLockEnabledFlow } returns flowOf(false)

        viewModel = SettingsViewModel(prefs, exportUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `exportState initial value is Idle`() = runTest {
        assertEquals(ExportUiState.Idle, viewModel.exportState.value)
    }

    @Test
    fun `exportJournal sets Loading state then Success on successful export`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { exportUseCase() } returns ExportJournalToPdfUseCase.Result.Success(mockUri, 5)

        viewModel.exportState.test {
            assertEquals(ExportUiState.Idle, awaitItem())

            viewModel.exportJournal()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(ExportUiState.Loading, awaitItem())
            val successState = awaitItem()
            assertTrue(successState is ExportUiState.Success)
            assertEquals(mockUri, (successState as ExportUiState.Success).uri)
            assertEquals(5, successState.entryCount)
        }
    }

    @Test
    fun `exportJournal sets NoEntries state when no entries exist`() = runTest {
        coEvery { exportUseCase() } returns ExportJournalToPdfUseCase.Result.NoEntries

        viewModel.exportState.test {
            assertEquals(ExportUiState.Idle, awaitItem())

            viewModel.exportJournal()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(ExportUiState.Loading, awaitItem())
            assertEquals(ExportUiState.NoEntries, awaitItem())
        }
    }

    @Test
    fun `exportJournal sets Error state on export failure`() = runTest {
        val errorMessage = "Failed to create PDF"
        coEvery { exportUseCase() } returns ExportJournalToPdfUseCase.Result.Error(errorMessage)

        viewModel.exportState.test {
            assertEquals(ExportUiState.Idle, awaitItem())

            viewModel.exportJournal()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(ExportUiState.Loading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is ExportUiState.Error)
            assertEquals(errorMessage, (errorState as ExportUiState.Error).message)
        }
    }

    @Test
    fun `resetExportState sets state back to Idle`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { exportUseCase() } returns ExportJournalToPdfUseCase.Result.Success(mockUri, 3)

        viewModel.exportState.test {
            assertEquals(ExportUiState.Idle, awaitItem())

            viewModel.exportJournal()
            testDispatcher.scheduler.advanceUntilIdle()

            skipItems(2)

            viewModel.resetExportState()
            assertEquals(ExportUiState.Idle, awaitItem())
        }
    }

    @Test
    fun `exportJournal calls exportUseCase`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { exportUseCase() } returns ExportJournalToPdfUseCase.Result.Success(mockUri, 1)

        viewModel.exportJournal()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { exportUseCase() }
    }

    @Test
    fun `multiple export calls work correctly`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { exportUseCase() } returns ExportJournalToPdfUseCase.Result.Success(mockUri, 2)

        viewModel.exportState.test {
            assertEquals(ExportUiState.Idle, awaitItem())

            viewModel.exportJournal()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(ExportUiState.Loading, awaitItem())
            assertTrue(awaitItem() is ExportUiState.Success)

            viewModel.resetExportState()
            assertEquals(ExportUiState.Idle, awaitItem())

            viewModel.exportJournal()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(ExportUiState.Loading, awaitItem())
            assertTrue(awaitItem() is ExportUiState.Success)
        }
    }

    @Test
    fun `setAccentColor calls prefs setAppThemeType`() = runTest {
        viewModel.setAccentColor("PURPLE")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { prefs.setAppThemeType(AppThemeType.PURPLE) }
    }

    @Test
    fun `setDynamicColor calls prefs setUseDynamicColor`() = runTest {
        viewModel.setDynamicColor(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { prefs.setUseDynamicColor(false) }
    }

    @Test
    fun `setDarkTheme calls prefs setDarkTheme`() = runTest {
        viewModel.setDarkTheme(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { prefs.setDarkTheme(true) }
    }

    @Test
    fun `setAppLockEnabled calls prefs setAppLockEnabled`() = runTest {
        viewModel.setAppLockEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { prefs.setAppLockEnabled(true) }
    }
}
