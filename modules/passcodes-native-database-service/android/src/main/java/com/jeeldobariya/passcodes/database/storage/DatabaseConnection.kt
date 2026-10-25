package com.jeeldobariya.passcodes.database.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * Opens the SQLite file managed by Expo SQLite / Drizzle. This module
 * never creates the file or runs migrations — if it doesn't exist yet,
 * the app hasn't been opened and there is nothing to read or write.
 *
 * Android applies its own default busy timeout (~2.5 s) to connections
 * opened this way, so no extra PRAGMA is needed to coexist with a
 * concurrent writer on the React Native side.
 */
internal class DatabaseConnection(context: Context) {

    // NOTE: this path is the one contract point between Expo SQLite and
    // this native module. If the RN side ever renames/relocates the
    // database, DATABASE_PATH is the single place to update.
    private val file = File(context.filesDir, DATABASE_PATH)

    fun exists(): Boolean = file.exists()

    fun open(readOnly: Boolean): SQLiteDatabase {
        val flags = if (readOnly) SQLiteDatabase.OPEN_READONLY else SQLiteDatabase.OPEN_READWRITE
        return SQLiteDatabase.openDatabase(file.absolutePath, null, flags)
    }

    private companion object {
        const val DATABASE_PATH = "SQLite/master.db"
    }
}
