package com.tobibur.journey.presentation.screens.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobibur.journey.data.local.datastore.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: SettingsPreferences
) : ViewModel() {

    val accentColor = prefs.accentColorFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Color(0xFF6750A4).toArgb()
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

    fun setAccentColor(color: Color) {
        viewModelScope.launch {
            prefs.setAccentColor(color.toArgb())
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
}
