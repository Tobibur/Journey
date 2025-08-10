package com.tobibur.journey.di

import android.app.Application
import androidx.room.Room
import com.tobibur.journey.data.local.dao.JournalDao
import com.tobibur.journey.data.local.database.JournalDatabase
import com.tobibur.journey.data.repository.JournalRepositoryImpl
import com.tobibur.journey.domain.repository.JournalRepository
import com.tobibur.journey.domain.usecase.AddEntryUseCase
import com.tobibur.journey.domain.usecase.DeleteEntryUseCase
import com.tobibur.journey.domain.usecase.GetEntryByIdUseCase
import com.tobibur.journey.domain.usecase.GetJournalEntriesUseCase
import com.tobibur.journey.domain.usecase.GetJournalStreakUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): JournalDatabase =
        Room.databaseBuilder(app, JournalDatabase::class.java, "journal_db")
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
}
