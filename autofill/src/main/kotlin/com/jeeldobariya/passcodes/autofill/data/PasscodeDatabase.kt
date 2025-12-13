package com.jeeldobariya.passcodes.autofill.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Passcode::class], version = 1, exportSchema = false)
abstract class PasscodeDatabase : RoomDatabase() {

    abstract fun passcodeDao(): PasscodeDao

    companion object {
        @Volatile
        private var INSTANCE: PasscodeDatabase? = null

        fun getDatabase(context: Context): PasscodeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PasscodeDatabase::class.java,
                    "passcode_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
