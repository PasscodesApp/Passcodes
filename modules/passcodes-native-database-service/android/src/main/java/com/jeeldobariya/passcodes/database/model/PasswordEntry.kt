package com.jeeldobariya.passcodes.database.model

/**
 * One row of the `passwords` table, as seen by native code.
 *
 * Timestamps are epoch seconds (Drizzle `integer` timestamps). Soft-deleted
 * rows are never mapped into this type — readers filter them out in SQL.
 */
data class PasswordEntry(
    val id: Long,
    val domain: String,
    val username: String,
    val password: String,
    val notes: String?,
    val url: String?,
    /** Comma-separated composite of package names / bundle ids; see AppIdentifierHelper. */
    val applicationIds: String?,
    /** Drizzle `source` enum: IMPORTED | APPLICATION | AUTOFILL. */
    val source: String?,
    val createdAt: Long,
    val updatedAt: Long
)
