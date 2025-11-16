package com.jeeldobariya.passcodes.oldui

sealed interface LoadPasswordAction {
    data object RefreshPassword: LoadPasswordAction
}
