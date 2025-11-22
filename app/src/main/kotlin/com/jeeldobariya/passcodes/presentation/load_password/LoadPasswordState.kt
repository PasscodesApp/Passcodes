package com.jeeldobariya.passcodes.presentation.load_password

import com.jeeldobariya.passcodes.database.Password

data class LoadPasswordState(
    val passwordEntityList: List<Password> = emptyList(),
    val isError: Boolean = false
)