package com.jeeldobariya.passcodes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.jeeldobariya.passcodes.core.datastore.AppSettings
import com.jeeldobariya.passcodes.core.datastore.appDatastore
import com.jeeldobariya.passcodes.core.navigation.Route
import com.jeeldobariya.passcodes.password_manager.ui.ClassicalLoadPasswordScreen
import com.jeeldobariya.passcodes.password_manager.ui.ClassicalPasswordManagerScreen
import com.jeeldobariya.passcodes.password_manager.ui.ClassicalSavePasswordScreen
import com.jeeldobariya.passcodes.password_manager.ui.ClassicalUpdatePasswordScreen
import com.jeeldobariya.passcodes.password_manager.ui.ClassicalViewPasswordScreen
import com.jeeldobariya.passcodes.password_manager.ui.ModernPasswordManagerScreen
import com.jeeldobariya.passcodes.password_manager.ui.ModernSavePasswordScreen
import com.jeeldobariya.passcodes.password_manager.ui.ModernUpdatePasswordScreen
import com.jeeldobariya.passcodes.ui.ClassicalAboutScreen
import com.jeeldobariya.passcodes.ui.ClassicalMainScreen
import com.jeeldobariya.passcodes.ui.ClassicalSettingsScreen
import com.jeeldobariya.passcodes.ui.ModernAboutScreen
import com.jeeldobariya.passcodes.ui.ModernMainScreen
import com.jeeldobariya.passcodes.ui.ModernSettingsScreen


@Composable
private fun ModernNavigationRoot(backStack: NavBackStack<NavKey>, navigateTo: (Route) -> Unit) {
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
                ModernMainScreen(navigateTo)
            }

            entry<Route.Settings> {
                ModernSettingsScreen()
            }

            entry<Route.AboutUs> {
                ModernAboutScreen()
            }

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
    )
}

@Composable
private fun ClassicalNavigationRoot(backStack: NavBackStack<NavKey>, navigateTo: (Route) -> Unit) {
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
                ClassicalMainScreen(navigateTo = navigateTo)
            }

            entry<Route.Settings> {
                ClassicalSettingsScreen()
            }

            entry<Route.AboutUs> {
                ClassicalAboutScreen()
            }

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
    )
}

@Composable
fun NavigationRoot() {
    val backStack = rememberNavBackStack(Route.Home)

    val appDataStore = LocalContext.current.appDatastore
    val appDatastoreState by appDataStore.data.collectAsState(AppSettings())

    fun navigateTo(route: Route) {
        backStack.add(route)
    }

    if (appDatastoreState.isModernLayoutEnable) { ModernNavigationRoot(backStack, ::navigateTo) } else {
        ClassicalNavigationRoot(backStack, ::navigateTo)
    }
}
