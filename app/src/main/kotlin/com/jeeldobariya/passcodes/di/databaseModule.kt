package com.jeeldobariya.passcodes.di

import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.database.MasterDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        MasterDatabase.createNewDatabase(androidContext())
    }

    single {
        PasswordRepository(get<MasterDatabase>().passwordsDao)
    }
}
