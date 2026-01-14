package com.jeeldobariya.passcodes.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route: NavKey {

    @Serializable
    data object Home: Route, NavKey

    @Serializable
    data object AboutUs: Route, NavKey

    @Serializable
    data object Settings: Route, NavKey

    @Serializable
    data object PasswordManager: Route, NavKey

    @Serializable
    data object SavePassword: Route, NavKey
}
