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
import com.jeeldobariya.passcodes.password_manager.ui.PasswordManagerScreen
import com.jeeldobariya.passcodes.password_manager.ui.SavePasswordScreen
import com.jeeldobariya.passcodes.password_manager.ui.UpdatePasswordScreen
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
                PasswordManagerScreen(navigateTo)
            }

            entry<Route.SavePassword> {
                SavePasswordScreen()
            }

            entry<Route.UpdatePassword> {
                UpdatePasswordScreen(it.id)
            }
        }
    )
}

@Composable
private fun ClassicNavigationRoot(backStack: NavBackStack<NavKey>, navigateTo: (Route) -> Unit) {
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
                ClassicalMainScreen(navigateTo)
            }

            entry<Route.Settings> {
                ClassicalSettingsScreen(
                    selectedLanguage = "Under Development",
                    languageOptions = listOf("English", "Korean"),
                    onLanguageSelected = { /* TODO */ },
                    onToggleTheme = { /* TODO */ },
                    onClearAllDataClick = { /* TODO */ }
                )
            }

            entry<Route.AboutUs> {
                ClassicalAboutScreen()
            }

            entry<Route.PasswordManager> {
                PasswordManagerScreen(navigateTo)
            }

            entry<Route.SavePassword> {
                SavePasswordScreen()
            }

            entry<Route.UpdatePassword> {
                UpdatePasswordScreen(it.id)
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

    if (appDatastoreState.isModernLayoutEnable) { ModernNavigationRoot(backStack, ::navigateTo) }
    else { ClassicNavigationRoot(backStack, ::navigateTo) }
}
