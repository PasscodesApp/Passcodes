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
        @Volatile
        private var INSTANCE: MasterDatabase? = null

        fun getDatabase(context: Context): MasterDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MasterDatabase::class.java,
                    "master"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
