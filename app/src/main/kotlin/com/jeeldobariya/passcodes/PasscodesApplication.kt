package com.jeeldobariya.passcodes

import android.app.Application
import com.jeeldobariya.passcodes.core.di.coreModule
import com.jeeldobariya.passcodes.database.di.databaseModule
import com.jeeldobariya.passcodes.password_manager.di.passwordManagerModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PasscodesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PasscodesApplication)
            modules(coreModule)
            modules(passwordManagerModule)
            modules(databaseModule)
        }
    }
}
