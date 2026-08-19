package com.jeeldobariya.passcodes.autofill

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Provides read-only access to the Passcodes SQLite database.
 *
 * The database itself is managed by Expo SQLite / Drizzle.
 * This helper only reads the existing SQLite database directly
 * from the native Autofill Service.
 */
class PasscodesDatabase(
    context: Context
) {

    private val databaseFile: File =
        File(
            context.filesDir,
            DATABASE_PATH
        )

    /**
     * Returns all password entries stored in Passcodes.
     *
     * The Autofill Service only needs read access, therefore
     * the database is opened using OPEN_READONLY.
     */
    fun getAllPasswords(): List<PasswordEntry> {

        if (!databaseFile.exists()) {
            return emptyList()
        }

        return try {
            openDatabase().use { database ->
                queryPasswords(database)
            }
        } catch (exception: Exception) {
            /*
             * Autofill should fail gracefully if the database
             * cannot be opened or queried.
             *
             * We intentionally do not expose database errors
             * to the Autofill framework.
             */
            emptyList()
        }
    }

    private fun openDatabase(): SQLiteDatabase {
        return SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
    }

    private fun queryPasswords(
        database: SQLiteDatabase
    ): List<PasswordEntry> {

        val passwords = mutableListOf<PasswordEntry>()

        database.query(
            TABLE_PASSWORDS,
            COLUMNS,
            null,
            null,
            null,
            null,
            ORDER_BY_ID
        ).use { cursor ->

            val idIndex =
                cursor.getColumnIndexOrThrow(COLUMN_ID)

            val domainIndex =
                cursor.getColumnIndexOrThrow(COLUMN_DOMAIN)

            val usernameIndex =
                cursor.getColumnIndexOrThrow(COLUMN_USERNAME)

            val passwordIndex =
                cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)

            val notesIndex =
                cursor.getColumnIndexOrThrow(COLUMN_NOTES)

            val urlIndex =
                cursor.getColumnIndexOrThrow(COLUMN_URL)

            while (cursor.moveToNext()) {
                passwords.add(
                    PasswordEntry(
                        id = cursor.getLong(idIndex),
                        domain = cursor.getString(domainIndex),
                        username = cursor.getString(usernameIndex),
                        password = cursor.getString(passwordIndex),
                        notes = cursor.getStringOrNull(notesIndex),
                        url = cursor.getStringOrNull(urlIndex)
                    )
                )
            }
        }

        return passwords
    }

    private fun android.database.Cursor.getStringOrNull(
        columnIndex: Int
    ): String? {
        return if (isNull(columnIndex)) {
            null
        } else {
            getString(columnIndex)
        }
    }

    data class PasswordEntry(
        val id: Long,
        val domain: String,
        val username: String,
        val password: String,
        val notes: String?,
        val url: String?
    )

    companion object {
        private const val DATABASE_PATH = "SQLite/master.db"

        private const val TABLE_PASSWORDS = "passwords"

        private const val COLUMN_ID = "id"
        private const val COLUMN_DOMAIN = "domain"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NOTES = "notes"
        private const val COLUMN_URL = "url"

        private val COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_DOMAIN,
            COLUMN_USERNAME,
            COLUMN_PASSWORD,
            COLUMN_NOTES,
            COLUMN_URL
        )

        private const val ORDER_BY_ID = "$COLUMN_ID ASC"
    }
}
