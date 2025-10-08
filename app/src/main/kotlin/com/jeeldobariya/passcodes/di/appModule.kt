package com.jeeldobariya.passcodes.di

import com.jeeldobariya.passcodes.ui.SavePasswordViewModel
import com.jeeldobariya.passcodes.ui.UpdatePasswordViewModel
import com.jeeldobariya.passcodes.utils.Controller
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        Controller(androidContext())
    }

    viewModel {
        UpdatePasswordViewModel(get())
    }

    viewModel {
        SavePasswordViewModel(get())
    }

}
