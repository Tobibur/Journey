package com.tobibur.journey.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room migrations for [JournalDatabase].
 *
 * How to add a migration when you change the schema:
 *  1. Bump `version` in the [JournalDatabase] @Database annotation (e.g. 1 -> 2).
 *  2. Build once so Room exports the new schema JSON to app/schemas/.../<version>.json,
 *     and commit that file.
 *  3. Add a Migration below describing the SQL to get from the old version to the new one.
 *  4. Add it to [ALL] so it is applied automatically (see AppModule's databaseBuilder).
 *  5. Add a test in MigrationTest covering the new step.
 *
 * Keep migrations append-only and never edit an already-released one.
 *
 * Example (uncomment and adapt when you first need it):
 *
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE journal_entries ADD COLUMN mood TEXT")
 *     }
 * }
 */
object JournalMigrations {

    /** All migrations, in order. Wired into the database builder via `.addMigrations(*ALL)`. */
    val ALL: Array<Migration> = arrayOf(
        // MIGRATION_1_2,
    )
}
