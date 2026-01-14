package com.jeeldobariya.passcodes.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.jeeldobariya.passcodes.core.navigation.Route
import com.jeeldobariya.passcodes.password_manager.ui.PasswordManagerScreen
import com.jeeldobariya.passcodes.ui.AboutScreen
import com.jeeldobariya.passcodes.ui.MainScreen
import com.jeeldobariya.passcodes.ui.SettingsScreen

@Composable
fun NavigationRoot() {
    val backStack = rememberNavBackStack(Route.Home)

    fun navigateTo(route: Route): Unit {
        backStack.add(route)
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryDecorators = mutableListOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Route.Home> {
                MainScreen(::navigateTo)
            }

            entry<Route.Settings> {
                SettingsScreen()
            }

            entry<Route.AboutUs> {
                AboutScreen()
            }

            entry<Route.PasswordManager> {
                PasswordManagerScreen()
            }
        }
    )
}
