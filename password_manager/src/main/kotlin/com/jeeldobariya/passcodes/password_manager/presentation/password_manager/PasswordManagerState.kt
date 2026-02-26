package com.jeeldobariya.passcodes.password_manager.presentation.password_manager

import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal

data class PasswordManagerState(
    val passwordEntityList: List<PasswordModal> = emptyList(),
    val isError: Boolean = false
)
