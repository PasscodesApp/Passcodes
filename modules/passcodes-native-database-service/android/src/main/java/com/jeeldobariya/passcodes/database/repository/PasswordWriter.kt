package com.jeeldobariya.passcodes.database.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.jeeldobariya.passcodes.database.schema.PasswordsSchema as S
import com.jeeldobariya.passcodes.database.util.AppIdentifierHelper
import com.jeeldobariya.passcodes.database.util.DomainHelper

/**
 * Write-side operations. Callers are responsible for the surrounding
 * transaction (see PasscodesDatabase) so several writes — e.g. a future
 * bulk import — can share one.
 */
internal object PasswordWriter {

    private data class ExistingRecord(
        val id: Long,
        val notes: String?,
        val url: String?,
        val applicationIds: String?
    )

    /**
     * Inserts a new autofill-originated credential, or updates the one
     * matched by (normalized domain, username). Returns the row id.
     */
    fun upsertFromAutofill(
        db: SQLiteDatabase,
        normalizedDomain: String,
        username: String,
        password: String,
        url: String?,
        packageId: String?
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
            val mergedAppIds = AppIdentifierHelper.merge(existing.applicationIds, packageId)

            db.execSQL(
                """
                UPDATE ${S.TABLE}
                SET ${S.PASSWORD} = ?,
                    ${S.URL} = ?,
                    ${S.NOTES} = ?,
                    ${S.APPLICATION_IDS} = ?,
                    ${S.UPDATED_AT} = (CAST(strftime('%s', 'now') AS INTEGER))
                WHERE ${S.ID} = ?
                """.trimIndent(),
                arrayOf(password, resolvedUrl, mergedNotes, mergedAppIds, existing.id)
            )
            existing.id
        } else {
            val resolvedUrl = explicitUrl ?: DomainHelper.buildFallbackUrl(normalizedDomain)
            val mergedAppIds = AppIdentifierHelper.merge(existing = null, identifier = packageId)

            val values = ContentValues().apply {
                put(S.DOMAIN, normalizedDomain)
                put(S.USERNAME, username)
                put(S.PASSWORD, password)
                put(S.URL, resolvedUrl)
                put(S.NOTES, S.NOTE_AUTOFILL_SAVED)
                put(S.SOURCE, S.SOURCE_AUTOFILL)
                if (mergedAppIds != null) put(S.APPLICATION_IDS, mergedAppIds)
                // created_at / updated_at / deleted_at use column defaults.
            }
            db.insertOrThrow(S.TABLE, null, values)
        }
    }

    /**
     * If duplicate (domain, username) rows ever exist — the schema has
     * no UNIQUE constraint enforcing this — the most recently updated,
     * non-deleted row wins deterministically rather than an arbitrary one.
     */
    private fun findExisting(db: SQLiteDatabase, normalizedDomain: String, username: String): ExistingRecord? {
        db.rawQuery(
            """
            SELECT ${S.ID}, ${S.NOTES}, ${S.URL}, ${S.APPLICATION_IDS}
            FROM ${S.TABLE}
            WHERE ${S.DOMAIN} = ? AND ${S.USERNAME} = ? AND ${S.DELETED_AT} IS NULL
            ORDER BY ${S.UPDATED_AT} DESC, ${S.ID} DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(normalizedDomain, username)
        ).use { c ->
            if (!c.moveToFirst()) return null
            return ExistingRecord(
                id = c.getLong(0),
                notes = c.stringOrNull(1),
                url = c.stringOrNull(2),
                applicationIds = c.stringOrNull(3)
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
            notes.startsWith(S.NOTE_AUTOFILL_SAVED) -> notes.removePrefix(S.NOTE_AUTOFILL_SAVED).trimStart('\n')
            notes.startsWith(S.NOTE_AUTOFILL_UPDATED) -> notes.removePrefix(S.NOTE_AUTOFILL_UPDATED).trimStart('\n')
            else -> notes
        }
        return if (userPortion.isBlank()) S.NOTE_AUTOFILL_UPDATED else "${S.NOTE_AUTOFILL_UPDATED}\n$userPortion"
    }
}
