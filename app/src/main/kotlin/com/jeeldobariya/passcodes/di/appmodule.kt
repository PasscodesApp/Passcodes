package com.jeeldobariya.passcodes.di

import com.jeeldobariya.passcodes.core.datastore.appDatastore
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.presentation.setting_screen.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        SettingsViewModel(
            appDatastore = androidContext().appDatastore,
            flagDataStore = androidContext().featureFlagsDatastore
        )
    }
}
