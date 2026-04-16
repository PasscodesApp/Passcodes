package com.jeeldobariya.passcodes.core.di

import com.jeeldobariya.passcodes.core.domain.usecases.CheckForUpdateUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {

    //TODO: It must be turn to single.. If in future it is required by multiple things and not just `CheckForUpdateUseCase()`.
    factory {
        HttpClient(CIO)
    }

    factory {
        CheckForUpdateUseCase(androidContext(), get())
    }
}
