package com.jeeldobariya.passcodes.presentation.setting_screen


sealed interface SettingsAction {
    data object TogglePreviewFeatures: SettingsAction
    data object TogglePreviewLayout: SettingsAction
    data object ToggleModernLayout: SettingsAction
    data object OnClearDataButtonClick: SettingsAction
}
