package com.jeeldobariya.passcodes.database.master.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
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

        db.execSQL("""
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

        db.execSQL("""
            UPDATE `new_table_passwords`
            SET `notes` = NULL
            WHERE `notes` = ''
        """.trimIndent())

        db.execSQL("""
            UPDATE `new_table_passwords`
            SET `url` = 'https://local.' || `domain`
            WHERE `url` IS NULL
        """.trimIndent())

        db.execSQL("DROP TABLE `passwords`")

        db.execSQL("ALTER TABLE `new_table_passwords` RENAME TO `passwords`")
    }
}
