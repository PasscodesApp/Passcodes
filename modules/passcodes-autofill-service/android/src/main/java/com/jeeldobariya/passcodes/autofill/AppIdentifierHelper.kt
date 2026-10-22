package com.jeeldobariya.passcodes.autofill

/**
 * Helpers for the `applicationIds` column: a comma-separated composite
 * value holding every Android package name and/or iOS bundle ID that
 * represents "the same app" for one credential, e.g.:
 *
 *   "com.outlook.ios,com.outlook.android"
 *
 * Kept separate from [DomainHelper] on purpose — domain normalization
 * and composite-id bookkeeping are different concerns with different
 * growth paths (e.g. this is where you'd add iOS bundle ID validation
 * later without touching domain logic at all).
 */
object AppIdentifierHelper {

    private const val DELIMITER = ","

    /** Splits a stored composite value into a clean, de-duplicated list. */
    fun parse(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(DELIMITER)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
    }

    /**
     * Appends [identifier] to [existing] if it isn't already present
     * (case-insensitive). Returns null if there's nothing to store.
     *
     * merge("com.outlook.ios", "com.outlook.android")
     *   -> "com.outlook.ios,com.outlook.android"
     */
    fun merge(existing: String?, identifier: String?): String? {
        val current = parse(existing)
        val toAdd = identifier?.trim()?.takeIf { it.isNotBlank() }

        val result = when {
            toAdd == null -> current
            current.any { it.equals(toAdd, ignoreCase = true) } -> current
            else -> current + toAdd
        }

        return result.takeIf { it.isNotEmpty() }?.joinToString(DELIMITER)
    }

    /**
     * SQL fragment matching one comma-delimited segment of [column]
     * exactly (not a loose substring match), case-insensitively.
     * Expects exactly one bind arg: the lowercased identifier to find.
     */
    fun containsSqlFragment(column: String): String =
        "((',' || LOWER($column) || ',') LIKE ('%,' || ? || ',%'))"
}
