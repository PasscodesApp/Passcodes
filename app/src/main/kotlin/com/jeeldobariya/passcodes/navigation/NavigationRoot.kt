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
import com.jeeldobariya.passcodes.password_manager.navigation.classicalPasswordManagerEntriesProvider
import com.jeeldobariya.passcodes.password_manager.navigation.modernPasswordManagerEntriesProvider


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
            modernMainAppEntriesProvider(navigateTo = navigateTo)
            modernPasswordManagerEntriesProvider(navigateTo = navigateTo)
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
            classicalMainAppEntriesProvider(navigateTo = navigateTo)
            classicalPasswordManagerEntriesProvider(navigateTo = navigateTo)
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
