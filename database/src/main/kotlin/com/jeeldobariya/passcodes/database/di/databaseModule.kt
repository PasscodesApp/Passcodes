package com.jeeldobariya.passcodes.database.di

import com.jeeldobariya.passcodes.database.master.MasterDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        MasterDatabase.createNewDatabase(androidContext())
    }

    single {
        get<MasterDatabase>().passwordsDao
    }
}
