package com.jeeldobariya.passcodes.di

import com.jeeldobariya.passcodes.domain.usecases.CheckForUpdateUseCase
import com.jeeldobariya.passcodes.domain.usecases.DeletePasswordUseCase
import com.jeeldobariya.passcodes.domain.usecases.EditPasswordUseCase
import com.jeeldobariya.passcodes.domain.usecases.ExportPasswordCSVUseCase
import com.jeeldobariya.passcodes.domain.usecases.ImportPasswordCSVUseCase
import com.jeeldobariya.passcodes.domain.usecases.RetrieveAllPasswordUseCase
import com.jeeldobariya.passcodes.domain.usecases.RetrievePasswordUseCase
import com.jeeldobariya.passcodes.domain.usecases.StorePasswordUseCase
import com.jeeldobariya.passcodes.presentation.load_password.LoadPasswordViewModel
import com.jeeldobariya.passcodes.presentation.save_password.SavePasswordViewModel
import com.jeeldobariya.passcodes.presentation.update_password.UpdatePasswordViewModel
import com.jeeldobariya.passcodes.presentation.view_password.ViewPasswordViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory {
        OkHttpClient()
    }

    factory {
        CheckForUpdateUseCase(androidContext(), get())
    }

    factory {
        ImportPasswordCSVUseCase(androidContext(), get())
    }

    factory {
        ExportPasswordCSVUseCase(androidContext(), get())
    }

    factory {
        StorePasswordUseCase(get())
    }

    factory {
        RetrievePasswordUseCase(get())
    }

    factory {
        RetrieveAllPasswordUseCase(get())
    }

    factory {
        EditPasswordUseCase(get())
    }

    factory {
        DeletePasswordUseCase(get())
    }

    viewModel {
        UpdatePasswordViewModel(get(), get())
    }

    viewModel {
        SavePasswordViewModel(get())
    }

    viewModel {
        LoadPasswordViewModel(get())
    }

    viewModel {
        ViewPasswordViewModel(get(), get())
    }

}
