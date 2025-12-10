package com.jeeldobariya.passcodes.password_manager.presentation.load_password

sealed interface LoadPasswordAction {
    data object RefreshPassword : LoadPasswordAction
}
