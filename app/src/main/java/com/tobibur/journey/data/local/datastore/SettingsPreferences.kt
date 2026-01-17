package com.tobibur.journey.data.local.datastore

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tobibur.journey.ui.theme.AppThemeType
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
    companion object {
        private val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val APP_THEME_TYPE = stringPreferencesKey("app_theme_type")
    }

    val useDynamicColorFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[USE_DYNAMIC_COLOR] ?: true }

    val darkThemeFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[DARK_THEME] ?: isSystemInDarkThemeDefault() }

    val appLockEnabledFlow: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[APP_LOCK_ENABLED] ?: false }

    val appThemeTypeFlow: Flow<AppThemeType> = dataStore.data
        .map { prefs ->
            val name = prefs[APP_THEME_TYPE] ?: AppThemeType.PINK.name
            try {
                AppThemeType.valueOf(name)
            } catch (e: IllegalArgumentException) {
                AppThemeType.PINK
            }
        }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[USE_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DARK_THEME] = enabled }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setAppThemeType(type: AppThemeType) {
        dataStore.edit { prefs -> prefs[APP_THEME_TYPE] = type.name }
    }

    private fun isSystemInDarkThemeDefault(): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}
