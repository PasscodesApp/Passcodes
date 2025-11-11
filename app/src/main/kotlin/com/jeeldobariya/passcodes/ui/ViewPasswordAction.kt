package com.jeeldobariya.passcodes.ui

sealed interface ViewPasswordAction {
    data class LoadPassswordData(val passwordId: Int): ViewPasswordAction
    data object RefreshPassswordData: ViewPasswordAction
    // data object NavigateUpdatePasswordAction: ViewPasswordAction
    data object DeletePasswordAction: ViewPasswordAction
}
