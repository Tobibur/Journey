package com.tobibur.journey.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.work.WorkManager
import com.tobibur.journey.data.local.dao.JournalDao
import com.tobibur.journey.data.local.database.JournalDatabase
import com.tobibur.journey.data.local.database.JournalMigrations
import com.tobibur.journey.data.local.datastore.dataStore
import com.tobibur.journey.data.repository.JournalRepositoryImpl
import com.tobibur.journey.domain.repository.JournalRepository
import com.tobibur.journey.domain.usecase.AddEntryUseCase
import com.tobibur.journey.domain.usecase.DeleteAllEntriesUseCase
import com.tobibur.journey.domain.usecase.DeleteEntryUseCase
import com.tobibur.journey.domain.usecase.GetEntryByIdUseCase
import com.tobibur.journey.domain.usecase.GetJournalEntriesUseCase
import com.tobibur.journey.domain.usecase.GetJournalStreakUseCase
import com.tobibur.journey.domain.usecase.ExportJournalToJsonUseCase
import com.tobibur.journey.domain.usecase.ExportJournalToPdfUseCase
import com.tobibur.journey.domain.usecase.ImportJournalFromJsonUseCase
import com.tobibur.journey.utils.JsonFileManager
import com.tobibur.journey.utils.JournalPdfGenerator
import com.tobibur.journey.utils.PdfFileManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): JournalDatabase =
        Room.databaseBuilder(app, JournalDatabase::class.java, "journal_db")
            // Apply real migrations; never silently wipe user entries on a
            // missing/failed migration (it throws loudly instead).
            .addMigrations(*JournalMigrations.ALL)
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    fun provideJournalDao(db: JournalDatabase): JournalDao = db.journalDao()

    @Provides
    fun provideJournalRepository(dao: JournalDao): JournalRepository =
        JournalRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideAddEntryUseCase(repository: JournalRepository): AddEntryUseCase {
        return AddEntryUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetJournalEntriesUseCase(repository: JournalRepository): GetJournalEntriesUseCase {
        return GetJournalEntriesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetJournalByIdEntriesUseCase(repository: JournalRepository): GetEntryByIdUseCase {
        return GetEntryByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteJournalByIdEntriesUseCase(repository: JournalRepository): DeleteEntryUseCase {
        return DeleteEntryUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetJournalStreakUseCase(repository: JournalRepository): GetJournalStreakUseCase {
        return GetJournalStreakUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideJournalPdfGenerator(): JournalPdfGenerator {
        return JournalPdfGenerator()
    }

    @Provides
    @Singleton
    fun providePdfFileManager(@ApplicationContext context: Context): PdfFileManager {
        return PdfFileManager(context)
    }

    @Provides
    @Singleton
    fun provideExportJournalToPdfUseCase(
        repository: JournalRepository,
        pdfGenerator: JournalPdfGenerator,
        pdfFileManager: PdfFileManager
    ): ExportJournalToPdfUseCase {
        return ExportJournalToPdfUseCase(repository, pdfGenerator, pdfFileManager)
    }

    @Provides
    @Singleton
    fun provideJsonFileManager(@ApplicationContext context: Context): JsonFileManager {
        return JsonFileManager(context)
    }

    @Provides
    @Singleton
    fun provideExportJournalToJsonUseCase(
        repository: JournalRepository,
        jsonFileManager: JsonFileManager
    ): ExportJournalToJsonUseCase {
        return ExportJournalToJsonUseCase(repository, jsonFileManager)
    }

    @Provides
    @Singleton
    fun provideImportJournalFromJsonUseCase(
        repository: JournalRepository,
        jsonFileManager: JsonFileManager
    ): ImportJournalFromJsonUseCase {
        return ImportJournalFromJsonUseCase(repository, jsonFileManager)
    }

    @Provides
    @Singleton
    fun provideDeleteAllEntriesUseCase(repository: JournalRepository): DeleteAllEntriesUseCase {
        return DeleteAllEntriesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): androidx.work.WorkManager {
        return WorkManager.getInstance(context)
    }
}
