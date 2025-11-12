package com.jeeldobariya.passcodes.ui

sealed interface ViewPasswordAction {
    data class LoadPassword(val passwordId: Int): ViewPasswordAction
    data object RefreshPassword: ViewPasswordAction
    // data object NavigateUpdatePasswordAction: ViewPasswordAction
    data object DeletePasswordAction: ViewPasswordAction
}
