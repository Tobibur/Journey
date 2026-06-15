package com.tobibur.journey.presentation.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExportUiState {
    data object Idle : ExportUiState()
    data object Loading : ExportUiState()
    data class Success(val uri: Uri, val entryCount: Int, val type: ExportType) : ExportUiState()
    data object NoEntries : ExportUiState()
    data class Error(val message: String) : ExportUiState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: SettingsPreferences,
    private val exportJournalToPdfUseCase: ExportJournalToPdfUseCase,
    private val exportJournalToJsonUseCase: ExportJournalToJsonUseCase,
    private val importJournalFromJsonUseCase: ImportJournalFromJsonUseCase,
    private val deleteEntryUseCase: DeleteAllEntriesUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState

    val appThemeType = prefs.appThemeTypeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppThemeType.PINK
    )

    val useDynamicColor = prefs.useDynamicColorFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        true
    )

    val darkThemeEnabled = prefs.darkThemeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val appLockEnabled = prefs.appLockEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val reminderEnabled = prefs.reminderEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val reminderTime = prefs.reminderTimeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        20 * 60
    )

    fun setAccentColor(appThemeType: String) {
        viewModelScope.launch {
            prefs.setAppThemeType(AppThemeType.valueOf(appThemeType))
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setUseDynamicColor(enabled)
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setDarkTheme(enabled)
        }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setAppLockEnabled(enabled)
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setReminderEnabled(enabled)
            if (enabled) {
                val time = reminderTime.value
                val hour = time / 60
                val minute = time % 60
                reminderScheduler.schedule(hour, minute)
            } else {
                reminderScheduler.cancel()
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            prefs.setReminderTime(hour, minute)
            // Reschedule with the new time only if reminders are currently on.
            if (reminderEnabled.value) {
                reminderScheduler.schedule(hour, minute)
            }
        }
    }

    fun exportPDFJournal() {
        viewModelScope.launch {
            _exportState.value = ExportUiState.Loading
            _exportState.value = when (val result = exportJournalToPdfUseCase()) {
                is ExportState.Success ->
                    ExportUiState.Success(result.uri, result.entryCount, ExportType.PDF)

                is ExportState.NoEntries ->
                    ExportUiState.NoEntries

                is ExportState.Error ->
                    ExportUiState.Error(result.message)
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportUiState.Idle
    }

    fun exportJsonJournal() {
        viewModelScope.launch {
            _exportState.value = ExportUiState.Loading
            _exportState.value = when (val result = exportJournalToJsonUseCase()) {
                is ExportState.Success ->
                    ExportUiState.Success(result.uri, result.entryCount, ExportType.JSON)

                is ExportState.NoEntries ->
                    ExportUiState.NoEntries

                is ExportState.Error ->
                    ExportUiState.Error(result.message)
            }
        }
    }

    fun importFromJson(bytes: ByteArray?) {
        _importState.value = ImportState.Loading
        viewModelScope.launch {
            if (bytes != null) {
                val result = importJournalFromJsonUseCase(bytes)
                // Handle ImportState result
                _importState.value = result
            }else{
                _importState.value = ImportState.Error("No file selected")
            }
        }
    }

    fun resetImportState() {
        _importState.value = ImportState.Idle
    }

    suspend fun deleteAllEntries(): Int {
        val deletedCount = viewModelScope.async {
            // Delete all entries
            deleteEntryUseCase()
        }

        return deletedCount.await()
    }
}
