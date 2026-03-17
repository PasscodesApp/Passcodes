package com.jeeldobariya.passcodes.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.jeeldobariya.passcodes.core.navigation.Route
import com.jeeldobariya.passcodes.presentation.about_screen.ClassicalAboutScreen
import com.jeeldobariya.passcodes.presentation.about_screen.ModernAboutScreen
import com.jeeldobariya.passcodes.presentation.main_screen.ClassicalMainScreen
import com.jeeldobariya.passcodes.presentation.main_screen.ModernMainScreen
import com.jeeldobariya.passcodes.presentation.setting_screen.ClassicalSettingsScreen
import com.jeeldobariya.passcodes.presentation.setting_screen.ModernSettingsScreen

fun EntryProviderScope<NavKey>.modernMainAppEntriesProvider(navigateTo: (Route) -> Unit) {
    entry<Route.Home> {
        ModernMainScreen(navigateTo)
    }

    entry<Route.Settings> {
        ModernSettingsScreen()
    }

    entry<Route.AboutUs> {
        ModernAboutScreen()
    }
}

fun EntryProviderScope<NavKey>.classicalMainAppEntriesProvider(navigateTo: (Route) -> Unit) {
    entry<Route.Home> {
        ClassicalMainScreen(navigateTo = navigateTo)
    }

    entry<Route.Settings> {
        ClassicalSettingsScreen()
    }

    entry<Route.AboutUs> {
        ClassicalAboutScreen()
    }
}
