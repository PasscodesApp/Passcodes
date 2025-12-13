package com.jeeldobariya.passcodes.password_manager.presentation.load_password

import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal

data class LoadPasswordState(
    val passwordEntityList: List<PasswordModal> = emptyList(),
    val isError: Boolean = false
)
