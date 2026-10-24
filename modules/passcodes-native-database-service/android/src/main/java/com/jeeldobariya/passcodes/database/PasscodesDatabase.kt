package com.jeeldobariya.passcodes.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.jeeldobariya.passcodes.database.model.PasswordEntry
import com.jeeldobariya.passcodes.database.repository.PasswordReader
import com.jeeldobariya.passcodes.database.repository.PasswordWriter
import com.jeeldobariya.passcodes.database.storage.DatabaseConnection
import com.jeeldobariya.passcodes.database.util.DomainHelper

/**
 * The public face of the native database module: the only class other
 * modules (autofill today; importer/exporter later) should touch.
 *
 * Native read/write access to the SQLite file managed by Expo SQLite /
 * Drizzle. Never runs migrations — only reads and writes rows in a
 * schema the RN side owns (mirrored in `schema/PasswordsSchema`).
 *
 * Every function is blocking and does file I/O: call from a background
 * thread / `Dispatchers.IO`, never the main thread. None of them throw —
 * failures are logged and surface as an empty/null/false result.
 */
class PasscodesDatabase(context: Context) {

    private val connection = DatabaseConnection(context.applicationContext)

    // -----------------------------------------------------------------
    // Reads
    // -----------------------------------------------------------------

    /** Every non-deleted credential, most recently updated first. */
    fun getAllPasswords(): List<PasswordEntry> =
        read("getAllPasswords", emptyList()) { PasswordReader.queryAll(it) }

    /**
     * Credentials relevant to [webDomain] and/or [packageName].
     *
     * - [webDomain] matches the `domain` column, letting a stored domain
     *   cover its own subdomains (stored "google.com" matches a request
     *   for "accounts.google.com") but not the reverse.
     * - [packageName] matches as an exact segment of the comma-delimited
     *   `application_ids` column — never against `domain`, which holds
     *   site/app display names, not package identifiers.
     *
     * [fallbackToAll] defaults to false: if the site/app can't be
     * identified, or nothing matches, you get nothing rather than the
     * entire vault. Flip it on only for an explicit "show everything if
     * unsure" behaviour (the Auto fill mode).
     */
    fun filterPasswords(
        webDomain: String?,
        packageName: String?,
        fallbackToAll: Boolean = false
    ): List<PasswordEntry> {
        val normalizedWebDomain = DomainHelper.normalizeDomain(webDomain)
        val normalizedPackage = packageName?.trim()?.lowercase()?.takeIf { it.isNotBlank() }

        return read("filterPasswords", emptyList()) { db ->
            if (normalizedWebDomain == null && normalizedPackage == null) {
                if (fallbackToAll) PasswordReader.queryAll(db) else emptyList()
            } else {
                val matches = PasswordReader.queryFiltered(db, normalizedWebDomain, normalizedPackage)
                if (matches.isEmpty() && fallbackToAll) PasswordReader.queryAll(db) else matches
            }
        }
    }

    /** One credential by row id (null if missing or soft-deleted). */
    fun getPasswordById(id: Long): PasswordEntry? =
        read("getPasswordById", null) { PasswordReader.queryById(it, id) }

    /** Number of non-deleted credentials. */
    fun countPasswords(): Int =
        read("countPasswords", 0) { PasswordReader.count(it) }

    // -----------------------------------------------------------------
    // Writes
    // -----------------------------------------------------------------

    /**
     * Inserts a new credential or updates the existing one matched by
     * (normalized domain, username). Rows written this way are flagged
     * as autofill-originated (`source` + a `notes` marker) so the user
     * can review them in-app.
     *
     * [packageId] — pass the reporting app's package name only when the
     * save came from a native app form (no web domain). It's merged into
     * `application_ids`; pass null for a web form, where the "app"
     * (e.g. the browser) isn't meaningfully tied to the credential.
     *
     * Returns the row id on success, null on failure.
     */
    fun savePassword(
        domain: String,
        username: String,
        password: String,
        url: String? = null,
        packageId: String? = null
    ): Long? {
        if (password.isBlank()) return null

        val normalizedDomain = DomainHelper.normalizeDomain(domain)
            ?: domain.trim().lowercase().takeIf { it.isNotBlank() }
            ?: return null
        val trimmedUsername = username.trim()

        return write("savePassword", null) { db ->
            PasswordWriter.upsertFromAutofill(db, normalizedDomain, trimmedUsername, password, url, packageId)
        }
    }

    // -----------------------------------------------------------------
    // Plumbing
    // -----------------------------------------------------------------

    private inline fun <T> read(operation: String, fallback: T, block: (SQLiteDatabase) -> T): T {
        if (!connection.exists()) return fallback
        return try {
            connection.open(readOnly = true).use(block)
        } catch (e: Exception) {
            Log.e(TAG, "$operation failed", e)
            fallback
        }
    }

    /** Runs [block] in a single transaction; any exception rolls it back. */
    private inline fun <T> write(operation: String, fallback: T, block: (SQLiteDatabase) -> T): T {
        if (!connection.exists()) return fallback
        return try {
            connection.open(readOnly = false).use { db ->
                db.beginTransaction()
                try {
                    val result = block(db)
                    db.setTransactionSuccessful()
                    result
                } finally {
                    db.endTransaction()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "$operation failed", e)
            fallback
        }
    }

    private companion object {
        const val TAG = "PasscodesDatabase"
    }
}
