package com.jeeldobariya.passcodes.ui

sealed interface LoadPasswordAction {
    data object RefreshPassword: LoadPasswordAction
}
