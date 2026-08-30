package com.jeeldobariya.passcodes.autofill

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File

/**
 * Native read/write access to the Passcodes SQLite database managed by
 * Expo SQLite / Drizzle. This class never runs migrations — only reads
 * and writes rows into a schema the RN side already owns.
 *
 * Public surface is intentionally just three functions:
 *  - getAllPasswords   : everything, unfiltered.
 *  - filterPasswords   : scoped to a site/app; empty by default if unsure.
 *  - savePassword      : upsert-by-(domain, username), autofill-flagged.
 */
class PasscodesDatabase(context: Context) {

    private val databaseFile = File(context.filesDir, DATABASE_PATH)

    // -----------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------

    fun getAllPasswords(): List<PasswordEntry> {
        if (!databaseFile.exists()) return emptyList()
        
        return try {
            openDatabase().use { db -> queryAll(db) }
        } catch (e: Exception) {
            Log.e(TAG, "getAllPasswords failed", e)
            emptyList()
        }
    }

    /**
     * Returns credentials relevant to [webDomain] and/or [packageName].
     * Matching allows a stored domain to cover its own subdomains (a
     * stored "google.com" matches a request for "accounts.google.com"),
     * but not the reverse — we don't surface a narrower stored entry
     * (e.g. "login.google.com") just because the request is broader.
     *
     * [fallbackToAll] defaults to false: if identity can't be resolved,
     * we show nothing rather than the entire vault. Only flip this on
     * for an explicit, user-initiated "show all credentials" action.
     */
    fun filterPasswords(
        webDomain: String?,
        packageName: String?,
        fallbackToAll: Boolean = false
    ): List<PasswordEntry> {
        if (!databaseFile.exists()) return emptyList()

        val normalizedWebDomain = DomainHelper.normalizeDomain(webDomain)
        val normalizedPackage = packageName?.trim()?.lowercase()?.takeIf { it.isNotBlank() }

        if (normalizedWebDomain == null && normalizedPackage == null) {
            return if (fallbackToAll) getAllPasswords() else emptyList()
        }

        return try {
            openDatabase().use { db ->
                val matches = queryFiltered(db, normalizedWebDomain, normalizedPackage)
                if (matches.isEmpty() && fallbackToAll) queryAll(db) else matches
            }
        } catch (e: Exception) {
            Log.e(TAG, "filterPasswords failed", e)
            emptyList()
        }
    }

    /**
     * Inserts a new credential or updates the existing one matched by
     * (normalized domain, username). Rows created/updated this way are
     * flagged in `notes` as autofill-originated so the user can review
     * them in-app. Returns the row id on success, null on failure.
     */
    fun savePassword(
        domain: String,
        username: String,
        password: String,
        url: String? = null
    ): Long? {
        if (!databaseFile.exists()) return null

        val trimmedUsername = username.trim()
        val normalizedDomain = DomainHelper.normalizeDomain(domain)
            ?: domain.trim().lowercase().takeIf { it.isNotBlank() }
            ?: return null

        if (password.isBlank()) return null

        return try {
            openDatabase(readOnly = false).use { db ->
                db.beginTransaction()
                try {
                    val id = upsert(db, normalizedDomain, trimmedUsername, password, url)
                    db.setTransactionSuccessful()
                    id
                } finally {
                    db.endTransaction()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "savePassword failed", e)
            null
        }
    }

    // -----------------------------------------------------------------
    // Save internals
    // -----------------------------------------------------------------

    private data class ExistingRecord(val id: Long, val notes: String?, val url: String?)

    private fun upsert(
        db: SQLiteDatabase,
        normalizedDomain: String,
        username: String,
        password: String,
        url: String?
    ): Long {
        val existing = findExisting(db, normalizedDomain, username)
        val explicitUrl = url?.trim()?.takeIf { it.isNotBlank() }

        return if (existing != null) {
            // An existing, real URL is never clobbered by an autofill-
            // derived one — the page the form happened to be on isn't
            // necessarily more authoritative than what's already saved.
            val resolvedUrl = existing.url?.takeIf { it.isNotBlank() }
                ?: explicitUrl
                ?: DomainHelper.buildFallbackUrl(normalizedDomain)
            val mergedNotes = mergeNotesForUpdate(existing.notes)

            db.execSQL(
                """
                UPDATE $TABLE_PASSWORDS
                SET $COLUMN_PASSWORD = ?,
                    $COLUMN_URL = ?,
                    $COLUMN_NOTES = ?,
                    $COLUMN_UPDATED_AT = CURRENT_TIMESTAMP
                WHERE $COLUMN_ID = ?
                """.trimIndent(),
                arrayOf(password, resolvedUrl, mergedNotes, existing.id)
            )
            existing.id
        } else {
            val resolvedUrl = explicitUrl ?: DomainHelper.buildFallbackUrl(normalizedDomain)
            val values = ContentValues().apply {
                put(COLUMN_DOMAIN, normalizedDomain)
                put(COLUMN_USERNAME, username)
                put(COLUMN_PASSWORD, password)
                put(COLUMN_URL, resolvedUrl)
                put(COLUMN_NOTES, NOTE_SAVED)
                // created_at / updated_at use the column defaults.
            }
            db.insertOrThrow(TABLE_PASSWORDS, null, values)
        }
    }

    /**
     * If duplicate (domain, username) rows ever exist — the schema has
     * no UNIQUE constraint enforcing this yet — the most recently
     * updated row wins deterministically, rather than an arbitrary one.
     */
    private fun findExisting(db: SQLiteDatabase, normalizedDomain: String, username: String): ExistingRecord? {
        db.rawQuery(
            "SELECT $COLUMN_ID, $COLUMN_NOTES, $COLUMN_URL FROM $TABLE_PASSWORDS " +
                "WHERE $COLUMN_DOMAIN = ? AND $COLUMN_USERNAME = ? " +
                "ORDER BY $COLUMN_UPDATED_AT DESC, $COLUMN_ID DESC LIMIT 1",
            arrayOf(normalizedDomain, username)
        ).use { c ->
            if (!c.moveToFirst()) return null
            return ExistingRecord(
                id = c.getLong(0),
                notes = c.getStringOrNull(1),
                url = c.getStringOrNull(2)
            )
        }
    }

    /**
     * Marks a row as autofill-updated without letting the marker stack
     * up on every save. If the existing notes already start with our
     * marker, it's replaced in place rather than prepended again.
     */
    private fun mergeNotesForUpdate(existingNotes: String?): String {
        val notes = existingNotes?.trim().orEmpty()
        val userPortion = when {
            notes.isEmpty() -> ""
            notes.startsWith(NOTE_SAVED) -> notes.removePrefix(NOTE_SAVED).trimStart('\n')
            notes.startsWith(NOTE_UPDATED) -> notes.removePrefix(NOTE_UPDATED).trimStart('\n')
            else -> notes
        }
        return if (userPortion.isBlank()) NOTE_UPDATED else "$NOTE_UPDATED\n$userPortion"
    }

    // -----------------------------------------------------------------
    // Read internals
    // -----------------------------------------------------------------

    private fun queryAll(db: SQLiteDatabase): List<PasswordEntry> =
        db.query(TABLE_PASSWORDS, COLUMNS, null, null, null, null, ORDER_BY_ID).use(::mapCursor)

    private fun queryFiltered(
        db: SQLiteDatabase,
        webDomain: String?,
        packageName: String?
    ): List<PasswordEntry> {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (webDomain != null) {
            conditions += "$COLUMN_DOMAIN = ?"
            args += webDomain
            // Request is a subdomain of a stored, broader domain —
            // e.g. stored "google.com" covers "accounts.google.com".
            // Deliberately NOT bidirectional: we don't match a stored
            // narrower entry against a broader request (see review notes).
            conditions += "? LIKE ('%.' || $COLUMN_DOMAIN)"
            args += webDomain
        }

        if (packageName != null) {
            conditions += "$COLUMN_DOMAIN = ?"
            args += packageName
        }

        val sql = "SELECT $COLUMNS_CSV FROM $TABLE_PASSWORDS WHERE ${conditions.joinToString(" OR ")} " +
            "ORDER BY $ORDER_BY_ID"

        return db.rawQuery(sql, args.toTypedArray()).use(::mapCursor)
    }

    private fun mapCursor(cursor: Cursor): List<PasswordEntry> {
        val idIdx = cursor.getColumnIndexOrThrow(COLUMN_ID)
        val domainIdx = cursor.getColumnIndexOrThrow(COLUMN_DOMAIN)
        val usernameIdx = cursor.getColumnIndexOrThrow(COLUMN_USERNAME)
        val passwordIdx = cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)
        val notesIdx = cursor.getColumnIndexOrThrow(COLUMN_NOTES)
        val urlIdx = cursor.getColumnIndexOrThrow(COLUMN_URL)
        val createdIdx = cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)
        val updatedIdx = cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)

        val list = mutableListOf<PasswordEntry>()
        while (cursor.moveToNext()) {
            list += PasswordEntry(
                id = cursor.getLong(idIdx),
                domain = cursor.getString(domainIdx),
                username = cursor.getString(usernameIdx),
                password = cursor.getString(passwordIdx),
                notes = cursor.getStringOrNull(notesIdx),
                url = cursor.getStringOrNull(urlIdx),
                createdAt = cursor.getStringOrNull(createdIdx),
                updatedAt = cursor.getStringOrNull(updatedIdx)
            )
        }
        return list
    }

    private fun Cursor.getStringOrNull(index: Int): String? = if (isNull(index)) null else getString(index)

    private fun openDatabase(readOnly: Boolean = true): SQLiteDatabase {
        val flags = if (readOnly) SQLiteDatabase.OPEN_READONLY else SQLiteDatabase.OPEN_READWRITE
        return SQLiteDatabase.openDatabase(
            databaseFile.absolutePath,
            null,
            flags
        )
    }

    data class PasswordEntry(
        val id: Long,
        val domain: String,
        val username: String,
        val password: String,
        val notes: String?,
        val url: String?,
        val createdAt: String?,
        val updatedAt: String?
    )

    companion object {
        private const val TAG = "PasscodesDatabase"

        // NOTE: this path is the one contract point between Expo SQLite
        // and this native reader. If the RN side ever renames/relocates
        // the database, this constant is the single place to update.
        private const val DATABASE_PATH = "SQLite/master.db"

        private const val TABLE_PASSWORDS = "passwords"

        private const val COLUMN_ID = "id"
        private const val COLUMN_DOMAIN = "domain"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NOTES = "notes"
        private const val COLUMN_URL = "url"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"

        private const val ORDER_BY_ID = "$COLUMN_ID DESC"

        private const val BUSY_TIMEOUT_MS = 5000

        private const val NOTE_SAVED = "saved using autofill; needs user review."
        private const val NOTE_UPDATED = "updated using autofill; needs user review."

        private val COLUMNS = arrayOf(
            COLUMN_ID, COLUMN_DOMAIN, COLUMN_USERNAME, COLUMN_PASSWORD,
            COLUMN_NOTES, COLUMN_URL, COLUMN_CREATED_AT, COLUMN_UPDATED_AT
        )
        private val COLUMNS_CSV = COLUMNS.joinToString(", ")
    }
}
