package com.jeeldobariya.passcodes.password_manager.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.jeeldobariya.passcodes.core.navigation.Route
import com.jeeldobariya.passcodes.password_manager.presentation.load_password.ClassicalLoadPasswordScreen
import com.jeeldobariya.passcodes.password_manager.presentation.password_manager.ClassicalPasswordManagerScreen
import com.jeeldobariya.passcodes.password_manager.presentation.password_manager.ModernPasswordManagerScreen
import com.jeeldobariya.passcodes.password_manager.presentation.save_password.ClassicalSavePasswordScreen
import com.jeeldobariya.passcodes.password_manager.presentation.save_password.ModernSavePasswordScreen
import com.jeeldobariya.passcodes.password_manager.presentation.update_password.ClassicalUpdatePasswordScreen
import com.jeeldobariya.passcodes.password_manager.presentation.update_password.ModernUpdatePasswordScreen
import com.jeeldobariya.passcodes.password_manager.presentation.view_password.ClassicalViewPasswordScreen

fun EntryProviderScope<NavKey>.modernPasswordManagerEntriesProvider(navigateTo: (Route) -> Unit) {
    entry<Route.PasswordManager> {
        ModernPasswordManagerScreen(navigateTo)
    }

    entry<Route.LoadPassword> {
        ModernPasswordManagerScreen(navigateTo)
    }

    entry<Route.SavePassword> {
        ModernSavePasswordScreen()
    }

    entry<Route.ViewPassword> { // Theoretically, Should be treated as `UpdatePassword` Route in Modern Layout...
        ModernUpdatePasswordScreen(it.id)
    }

    entry<Route.UpdatePassword> {
        ModernUpdatePasswordScreen(it.id)
    }
}

fun EntryProviderScope<NavKey>.classicalPasswordManagerEntriesProvider(navigateTo: (Route) -> Unit) {
    entry<Route.PasswordManager> {
        ClassicalPasswordManagerScreen(navigateTo = navigateTo)
    }

    entry<Route.LoadPassword> {
        ClassicalLoadPasswordScreen(navigateTo = navigateTo)
    }

    entry<Route.SavePassword> {
        ClassicalSavePasswordScreen()
    }

    entry<Route.ViewPassword> {
        ClassicalViewPasswordScreen(passwordId = it.id, navigateTo = navigateTo)
    }

    entry<Route.UpdatePassword> {
        ClassicalUpdatePasswordScreen(passwordId = it.id)
    }
}
