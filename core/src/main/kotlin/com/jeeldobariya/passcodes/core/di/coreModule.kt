package com.jeeldobariya.passcodes.core.di

import com.jeeldobariya.passcodes.core.domain.usecases.CheckForUpdateUseCase
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    factory {
        CheckForUpdateUseCase(androidContext(), OkHttpClient())
    }
}
