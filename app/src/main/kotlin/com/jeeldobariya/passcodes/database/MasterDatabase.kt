package com.jeeldobariya.passcodes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Password::class],
    version = 1,
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
            ).build()
        }
    }
}
