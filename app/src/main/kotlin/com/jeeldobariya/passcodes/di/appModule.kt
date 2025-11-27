package com.jeeldobariya.passcodes.di

import com.jeeldobariya.passcodes.domain.usecases.ExportPasswordCSVUseCase
import com.jeeldobariya.passcodes.domain.usecases.ImportPasswordCSVUseCase
import com.jeeldobariya.passcodes.presentation.load_password.LoadPasswordViewModel
import com.jeeldobariya.passcodes.presentation.save_password.SavePasswordViewModel
import com.jeeldobariya.passcodes.presentation.update_password.UpdatePasswordViewModel
import com.jeeldobariya.passcodes.presentation.view_password.ViewPasswordViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory {
        ImportPasswordCSVUseCase(androidContext(), get())
    }

    factory {
        ExportPasswordCSVUseCase(androidContext(), get())
    }

    viewModel {
        UpdatePasswordViewModel(get())
    }

    viewModel {
        SavePasswordViewModel(get())
    }

    viewModel {
        LoadPasswordViewModel(get())
    }

    viewModel {
        ViewPasswordViewModel(get())
    }

}
