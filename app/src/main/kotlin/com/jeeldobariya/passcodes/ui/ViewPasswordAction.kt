package com.jeeldobariya.passcodes.ui

sealed interface ViewPasswordAction {
    data class loadPassswordData(val passwordId: Int): ViewPasswordAction
    data object refreshPassswordData: ViewPasswordAction
    // data object navigateUpdatePasswordAction: ViewPasswordAction
    data object deletePasswordAction: ViewPasswordAction
}
