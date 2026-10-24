package com.jeeldobariya.passcodes.database.schema

/**
 * The native mirror of the Drizzle `passwords` table. The React Native
 * side owns the schema and migrations; this file is the single place
 * native code learns table/column names, so a schema change means
 * editing exactly one file here.
 */
internal object PasswordsSchema {

    const val TABLE = "passwords"

    const val ID = "id"
    const val DOMAIN = "domain"
    const val USERNAME = "username"
    const val PASSWORD = "password"
    const val NOTES = "notes"
    const val URL = "url"
    const val APPLICATION_IDS = "application_ids"
    const val SOURCE = "source"
    const val CREATED_AT = "created_at"
    const val UPDATED_AT = "updated_at"
    const val DELETED_AT = "deleted_at"

    /** Newest first; id breaks ties deterministically. */
    const val ORDER_BY = "$UPDATED_AT DESC, $ID DESC"

    val READ_COLUMNS = arrayOf(
        ID, DOMAIN, USERNAME, PASSWORD, NOTES, URL,
        APPLICATION_IDS, SOURCE, CREATED_AT, UPDATED_AT
    )
    val READ_COLUMNS_CSV = READ_COLUMNS.joinToString(", ")

    // Matches the Drizzle `source` enum: IMPORTED | APPLICATION | AUTOFILL.
    const val SOURCE_AUTOFILL = "AUTOFILL"

    const val NOTE_AUTOFILL_SAVED = "saved using autofill; needs user review."
    const val NOTE_AUTOFILL_UPDATED = "updated using autofill; needs user review."
}
