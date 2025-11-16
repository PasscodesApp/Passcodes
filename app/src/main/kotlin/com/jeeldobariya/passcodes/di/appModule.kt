package com.jeeldobariya.passcodes.di

import com.jeeldobariya.passcodes.oldui.LoadPasswordViewModel
import com.jeeldobariya.passcodes.oldui.SavePasswordViewModel
import com.jeeldobariya.passcodes.oldui.UpdatePasswordViewModel
import com.jeeldobariya.passcodes.oldui.ViewPasswordViewModel
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

    viewModel {
        LoadPasswordViewModel(get())
    }

    viewModel {
        ViewPasswordViewModel(get())
    }

}
