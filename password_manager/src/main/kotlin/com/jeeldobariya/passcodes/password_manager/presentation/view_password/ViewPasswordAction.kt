package com.jeeldobariya.passcodes.password_manager.presentation.view_password

sealed interface ViewPasswordAction {
    data class LoadPassword(val passwordId: Int) : ViewPasswordAction
    data object RefreshPassword : ViewPasswordAction

    // data object NavigateUpdatePasswordAction: ViewPasswordAction
    data object DeletePasswordAction : ViewPasswordAction
}
