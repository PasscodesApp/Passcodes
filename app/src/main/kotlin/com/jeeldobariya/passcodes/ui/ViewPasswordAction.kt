package com.jeeldobariya.passcodes.ui

sealed interface ViewPasswordAction {
    // data object navigateUpdatePasswordAction: ViewPasswordAction
    data object deletePasswordAction: ViewPasswordAction
}
