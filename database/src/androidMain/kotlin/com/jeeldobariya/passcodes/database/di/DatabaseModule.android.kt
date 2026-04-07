package com.jeeldobariya.passcodes.database.di

import androidx.room.Room
import com.jeeldobariya.passcodes.database.master.MasterDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val databaseModule = module {
    single {
        MasterDatabase.createNewDatabase(
            getDatabaseBuilder = {
                val appContext = androidContext()
                val dbFile = appContext.getDatabasePath("master")

                Room.databaseBuilder<MasterDatabase>(
                    context = appContext,
                    name = dbFile.absolutePath
                )
            }
        )
    }

    single {
        get<MasterDatabase>().passwordsDao
    }
}
