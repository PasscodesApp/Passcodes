package com.jeeldobariya.passcodes.database.master.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.jeeldobariya.passcodes.database.master.MasterDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MasterDatabaseMigration1To2Test {

    private val TEST_DB = "migration-1-2-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MasterDatabase::class.java
    )

    @Test
    fun migrate_basicData() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("""
                INSERT INTO passwords (id, domain, username, password, notes)
                VALUES (1, 'example.com', 'user', 'pass', 'note')
            """)
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 2, true, MIGRATION_1_2
        )

        val cursor = migratedDb.query("SELECT * FROM passwords")
        assertThat(cursor.count).isEqualTo(1)
        cursor.close()
    }

    @Test
    fun migrate_emptyNotes_becomesNull() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("""
                INSERT INTO passwords (id, domain, username, password, notes)
                VALUES (1, 'd', 'u', 'p', '')
            """)
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        val cursor = db.query("SELECT notes FROM passwords WHERE id = 1")
        cursor.moveToFirst()

        assertThat(cursor.isNull(0)).isTrue()

        cursor.close()
    }

    @Test
    fun migrate_urlGenerated() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("""
                INSERT INTO passwords (id, domain, username, password, notes)
                VALUES (1, 'google.com', 'u', 'p', 'n')
            """)
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        val cursor = db.query("SELECT url FROM passwords WHERE id = 1")
        cursor.moveToFirst()

        assertThat(cursor.getString(0))
            .isEqualTo("https://local.google.com")

        cursor.close()
    }
}
