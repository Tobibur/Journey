package com.tobibur.journey.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Migration tests for [JournalDatabase], backed by the exported schema JSON in app/schemas.
 *
 * When you add a migration, add a test like the commented [migrate1To2] below: open the DB at
 * the old version, insert representative rows, run the migration, then assert the data survived
 * and the new schema is valid.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        JournalDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    /** Sanity check that the current baseline schema (v1) can be created from the exported JSON. */
    @Test
    @Throws(IOException::class)
    fun createsLatestSchema() {
        // Keep in sync with the version in @Database on JournalDatabase.
        helper.createDatabase(testDb, 1).close()
    }

    /*
     * Template for the first real migration. Uncomment and adapt once MIGRATION_1_2 exists.
     *
     * @Test
     * @Throws(IOException::class)
     * fun migrate1To2() {
     *     helper.createDatabase(testDb, 1).apply {
     *         execSQL(
     *             "INSERT INTO journal_entries (id, title, content, timestamp) " +
     *                 "VALUES (1, 'Title', 'Content', 0)"
     *         )
     *         close()
     *     }
     *
     *     val db = helper.runMigrationsAndValidate(testDb, 2, true, JournalMigrations.MIGRATION_1_2)
     *
     *     db.query("SELECT title FROM journal_entries WHERE id = 1").use { cursor ->
     *         assert(cursor.moveToFirst())
     *         assert(cursor.getString(0) == "Title")
     *     }
     * }
     */
}
