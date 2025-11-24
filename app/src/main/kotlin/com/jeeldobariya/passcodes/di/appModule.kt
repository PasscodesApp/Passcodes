package com.jeeldobariya.passcodes.di

import com.jeeldobariya.passcodes.presentation.load_password.LoadPasswordViewModel
import com.jeeldobariya.passcodes.presentation.save_password.SavePasswordViewModel
import com.jeeldobariya.passcodes.presentation.update_password.UpdatePasswordViewModel
import com.jeeldobariya.passcodes.presentation.view_password.ViewPasswordViewModel
import com.jeeldobariya.passcodes.utils.Controller
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        Controller(get(), get())
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
