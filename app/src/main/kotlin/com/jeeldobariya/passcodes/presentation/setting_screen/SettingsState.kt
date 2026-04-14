package com.jeeldobariya.passcodes.presentation.setting_screen

data class SettingsState(
    val selectedLanguage: String = "Under Development",
    val languageOptions: List<String> = listOf("English", "Korean")
)
