package com.tobibur.journey.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tobibur.journey.data.local.datastore.SettingsPreferences
import com.tobibur.journey.ui.theme.AppThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: SettingsPreferences
) : ViewModel() {

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
}
