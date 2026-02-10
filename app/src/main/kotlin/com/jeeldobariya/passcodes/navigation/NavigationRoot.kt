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
import com.jeeldobariya.passcodes.core.feature_flags.FeatureFlagsSettings
import com.jeeldobariya.passcodes.core.feature_flags.featureFlagsDatastore
import com.jeeldobariya.passcodes.core.navigation.Route
import com.jeeldobariya.passcodes.password_manager.ui.PasswordManagerScreen
import com.jeeldobariya.passcodes.password_manager.ui.SavePasswordScreen
import com.jeeldobariya.passcodes.password_manager.ui.UpdatePasswordScreen
import com.jeeldobariya.passcodes.ui.AboutScreen
import com.jeeldobariya.passcodes.ui.ClassicalMainScreen
import com.jeeldobariya.passcodes.ui.MainScreen
import com.jeeldobariya.passcodes.ui.SettingsScreen


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
                MainScreen(navigateTo)
            }

            entry<Route.Settings> {
                SettingsScreen()
            }

            entry<Route.AboutUs> {
                AboutScreen()
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
                SettingsScreen()
            }

            entry<Route.AboutUs> {
                AboutScreen()
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

    val flagDataStore = LocalContext.current.featureFlagsDatastore
    val flagDatastoreState by flagDataStore.data.collectAsState(
        FeatureFlagsSettings(
            version = 0,
            isPreviewFeaturesEnabled = false,
            isPreviewLayoutEnabled = false
        )
    )

    fun navigateTo(route: Route): Unit {
        backStack.add(route)
    }

    if (flagDatastoreState.isPreviewLayoutEnabled) { ModernNavigationRoot(backStack, ::navigateTo) }
    else { ClassicNavigationRoot(backStack, ::navigateTo) }
}
