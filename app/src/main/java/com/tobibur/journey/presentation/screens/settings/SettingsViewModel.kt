package com.tobibur.journey.presentation.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobibur.journey.data.ExportState
import com.tobibur.journey.data.ExportType
import com.tobibur.journey.data.ImportState
import com.tobibur.journey.data.local.datastore.SettingsPreferences
import com.tobibur.journey.domain.usecase.ExportJournalToJsonUseCase
import com.tobibur.journey.domain.usecase.ExportJournalToPdfUseCase
import com.tobibur.journey.domain.usecase.ImportJournalFromJsonUseCase
import com.tobibur.journey.ui.theme.AppThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val importJournalFromJsonUseCase: ImportJournalFromJsonUseCase
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
}
