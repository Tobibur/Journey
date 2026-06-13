package com.tobibur.journey.di

import com.tobibur.journey.data.local.datastore.SettingsPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThemeBootstrapEntryPoint {
    fun settingsPreferences(): SettingsPreferences
}
