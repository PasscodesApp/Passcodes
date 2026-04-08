package com.jeeldobariya.passcodes.database.master.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS `new_table_passwords` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `domain` TEXT NOT NULL,
                `username` TEXT NOT NULL,
                `password` TEXT NOT NULL,
                `notes` TEXT,
                `url` TEXT,
                `created_at` TEXT DEFAULT CURRENT_TIMESTAMP,
                `updated_at` TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent())

        connection.execSQL("""
            INSERT INTO `new_table_passwords`
                (`id`, `domain`, `username`, `password`, `notes`, `created_at`, `updated_at`)
            SELECT
                `id`,
                `domain`,
                `username`,
                `password`,
                `notes`,
                `created_at`,
                `updated_at`
            FROM `passwords`
        """.trimIndent())

        connection.execSQL("""
            UPDATE `new_table_passwords`
            SET `notes` = NULL
            WHERE `notes` = ''
        """.trimIndent())

        connection.execSQL("""
            UPDATE `new_table_passwords`
            SET `url` = 'https://local.' || `domain`
            WHERE `url` IS NULL
        """.trimIndent())

        connection.execSQL("DROP TABLE `passwords`")

        connection.execSQL("ALTER TABLE `new_table_passwords` RENAME TO `passwords`")
    }
}
