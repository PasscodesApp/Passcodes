package com.jeeldobariya.passcodes.ui

import com.jeeldobariya.passcodes.database.Password

data class LoadPasswordState(
    val passwordEntityList: List<Password> = emptyList<Password>(),
    val isError: Boolean = false
)
