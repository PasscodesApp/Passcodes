package com.jeeldobariya.passcodes.presentation.setting_screen

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.core.datastore.AppSettings
import com.jeeldobariya.passcodes.core.datastore.appDatastore
import com.jeeldobariya.passcodes.core.feature_flags.FeatureFlagsSettings
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val appDatastore: DataStore<AppSettings>,
    private val flagDataStore: DataStore<FeatureFlagsSettings>
): ViewModel() {

    // private val _state = MutableStateFlow(SettingsState())
    // val state = _state.asStateFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.TogglePreviewFeatures -> {
                viewModelScope.launch {
                    flagDataStore.updateData {
                        it.copy(isPreviewFeaturesEnabled = !it.isPreviewFeaturesEnabled)
                    }
                }
            }
            SettingsAction.TogglePreviewLayout -> {
                viewModelScope.launch {
                    flagDataStore.updateData {
                        it.copy(isPreviewLayoutEnabled = !it.isPreviewLayoutEnabled)
                    }
                }
            }
            SettingsAction.ToggleModernLayout -> {
                viewModelScope.launch {
                    appDatastore.updateData {
                        it.copy(isModernLayoutEnable = !it.isModernLayoutEnable)
                    }
                }
            }
            SettingsAction.OnClearDataButtonClick -> TODO()
        }
    }
}
