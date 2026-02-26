package com.jeeldobariya.passcodes.password_manager.presentation.password_manager

sealed interface PasswordManagerAction {
    data object RefreshPassword : PasswordManagerAction
}
