package com.jeeldobariya.passcodes

import android.app.Application
import com.jeeldobariya.passcodes.di.appModule
import com.jeeldobariya.passcodes.database.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PasscodesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PasscodesApplication)
            modules(appModule)
            modules(databaseModule)
        }
    }
}
