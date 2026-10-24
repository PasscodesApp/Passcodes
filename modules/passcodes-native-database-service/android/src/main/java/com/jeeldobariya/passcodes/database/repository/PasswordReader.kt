package com.jeeldobariya.passcodes.database.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.jeeldobariya.passcodes.database.model.PasswordEntry
import com.jeeldobariya.passcodes.database.schema.PasswordsSchema as S
import com.jeeldobariya.passcodes.database.util.AppIdentifierHelper

/**
 * Read-side queries. Every query excludes soft-deleted rows
 * (`deleted_at IS NOT NULL`) and orders newest-updated first.
 */
internal object PasswordReader {

    fun queryAll(db: SQLiteDatabase): List<PasswordEntry> =
        db.query(S.TABLE, S.READ_COLUMNS, "${S.DELETED_AT} IS NULL", null, null, null, S.ORDER_BY)
            .use(::mapCursor)

    fun queryById(db: SQLiteDatabase, id: Long): PasswordEntry? =
        db.rawQuery(
            "SELECT ${S.READ_COLUMNS_CSV} FROM ${S.TABLE} " +
                "WHERE ${S.ID} = ? AND ${S.DELETED_AT} IS NULL LIMIT 1",
            arrayOf(id.toString())
        ).use { mapCursor(it).firstOrNull() }

    fun count(db: SQLiteDatabase): Int =
        db.rawQuery("SELECT COUNT(*) FROM ${S.TABLE} WHERE ${S.DELETED_AT} IS NULL", null)
            .use { if (it.moveToFirst()) it.getInt(0) else 0 }

    /**
     * Rows matching a (normalized) web domain and/or (lowercased)
     * package name. Both inputs are optional but at least one must be
     * non-null — callers guard that.
     */
    fun queryFiltered(
        db: SQLiteDatabase,
        webDomain: String?,
        packageName: String?
    ): List<PasswordEntry> {
        val orConditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (webDomain != null) {
            orConditions += "${S.DOMAIN} = ?"
            args += webDomain

            // Request is a subdomain of a stored, broader domain — e.g.
            // stored "google.com" covers "accounts.google.com". NOT
            // bidirectional. Written with substr() instead of LIKE so a
            // stored domain containing `_` can't act as a wildcard.
            orConditions += "(length(${S.DOMAIN}) > 0 AND " +
                "substr(?, -(length(${S.DOMAIN}) + 1)) = ('.' || ${S.DOMAIN}))"
            args += webDomain
        }

        if (packageName != null) {
            orConditions += AppIdentifierHelper.containsSqlFragment(S.APPLICATION_IDS)
            args += packageName
        }

        val sql = """
            SELECT ${S.READ_COLUMNS_CSV} FROM ${S.TABLE}
            WHERE ${S.DELETED_AT} IS NULL AND (${orConditions.joinToString(" OR ")})
            ORDER BY ${S.ORDER_BY}
        """.trimIndent()

        return db.rawQuery(sql, args.toTypedArray()).use(::mapCursor)
    }

    private fun mapCursor(cursor: Cursor): List<PasswordEntry> {
        val idIdx = cursor.getColumnIndexOrThrow(S.ID)
        val domainIdx = cursor.getColumnIndexOrThrow(S.DOMAIN)
        val usernameIdx = cursor.getColumnIndexOrThrow(S.USERNAME)
        val passwordIdx = cursor.getColumnIndexOrThrow(S.PASSWORD)
        val notesIdx = cursor.getColumnIndexOrThrow(S.NOTES)
        val urlIdx = cursor.getColumnIndexOrThrow(S.URL)
        val appIdsIdx = cursor.getColumnIndexOrThrow(S.APPLICATION_IDS)
        val sourceIdx = cursor.getColumnIndexOrThrow(S.SOURCE)
        val createdIdx = cursor.getColumnIndexOrThrow(S.CREATED_AT)
        val updatedIdx = cursor.getColumnIndexOrThrow(S.UPDATED_AT)

        val list = mutableListOf<PasswordEntry>()
        while (cursor.moveToNext()) {
            list += PasswordEntry(
                id = cursor.getLong(idIdx),
                domain = cursor.getString(domainIdx),
                username = cursor.getString(usernameIdx),
                password = cursor.getString(passwordIdx),
                notes = cursor.stringOrNull(notesIdx),
                url = cursor.stringOrNull(urlIdx),
                applicationIds = cursor.stringOrNull(appIdsIdx),
                source = cursor.stringOrNull(sourceIdx),
                createdAt = cursor.getLong(createdIdx),
                updatedAt = cursor.getLong(updatedIdx)
            )
        }
        return list
    }
}

internal fun Cursor.stringOrNull(index: Int): String? = if (isNull(index)) null else getString(index)
