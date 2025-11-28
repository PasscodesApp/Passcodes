package com.jeeldobariya.passcodes.presentation.load_password

import com.jeeldobariya.passcodes.domain.modals.PasswordModal

data class LoadPasswordState(
    val passwordEntityList: List<PasswordModal> = emptyList(),
    val isError: Boolean = false
)
