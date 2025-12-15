package com.jeeldobariya.passcodes.password_manager.di

import com.jeeldobariya.passcodes.password_manager.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.password_manager.domain.usecases.DeletePasswordUseCase
import com.jeeldobariya.passcodes.password_manager.domain.usecases.EditPasswordUseCase
import com.jeeldobariya.passcodes.password_manager.domain.usecases.ExportPasswordCSVUseCase
import com.jeeldobariya.passcodes.password_manager.domain.usecases.ImportPasswordCSVUseCase
import com.jeeldobariya.passcodes.password_manager.domain.usecases.RetrieveAllPasswordUseCase
import com.jeeldobariya.passcodes.password_manager.domain.usecases.RetrievePasswordUseCase
import com.jeeldobariya.passcodes.password_manager.domain.usecases.StorePasswordUseCase
import com.jeeldobariya.passcodes.password_manager.presentation.load_password.LoadPasswordViewModel
import com.jeeldobariya.passcodes.password_manager.presentation.save_password.SavePasswordViewModel
import com.jeeldobariya.passcodes.password_manager.presentation.update_password.UpdatePasswordViewModel
import com.jeeldobariya.passcodes.password_manager.presentation.view_password.ViewPasswordViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val passwordManagerModule = module {

    single {
        PasswordRepository(get())
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
