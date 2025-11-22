package com.jeeldobariya.passcodes.presentation.load_password

sealed interface LoadPasswordAction {
    data object RefreshPassword: LoadPasswordAction
}
