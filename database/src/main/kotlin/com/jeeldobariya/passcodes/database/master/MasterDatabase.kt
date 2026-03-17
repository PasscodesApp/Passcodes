package com.jeeldobariya.passcodes.database.master

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jeeldobariya.passcodes.database.master.migration.MIGRATION_1_2

@Database(
    entities = [PasswordEntity::class],
    version = 2,
    exportSchema = true
)
abstract class MasterDatabase : RoomDatabase() {
    abstract val passwordsDao: PasswordsDao

    companion object {
        fun createNewDatabase(context: Context): MasterDatabase {
            return Room.databaseBuilder(
                context = context,
                klass = MasterDatabase::class.java,
                name = "master"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
