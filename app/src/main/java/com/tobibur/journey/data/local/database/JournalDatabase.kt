package com.tobibur.journey.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tobibur.journey.data.local.dao.JournalDao
import com.tobibur.journey.data.local.entity.JournalEntity

@Database(entities = [JournalEntity::class], version = 1, exportSchema = false)
abstract class JournalDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
}
