package com.tobibur.journey.data.local.datastore

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    private val ACCENT_COLOR = intPreferencesKey("accent_color")
    private val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
    private val DARK_THEME = booleanPreferencesKey("dark_theme")

    val accentColorFlow: Flow<Int> = dataStore.data
        .map { prefs -> prefs[ACCENT_COLOR] ?: Color(0xFF6750A4).toArgb() } // default purple

    val useDynamicColorFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[USE_DYNAMIC_COLOR] ?: true }

    val darkThemeFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[DARK_THEME] ?: isSystemInDarkThemeDefault() }

    suspend fun setAccentColor(colorInt: Int) {
        dataStore.edit { prefs -> prefs[ACCENT_COLOR] = colorInt }
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[USE_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DARK_THEME] = enabled }
    }

    private fun isSystemInDarkThemeDefault(): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}
