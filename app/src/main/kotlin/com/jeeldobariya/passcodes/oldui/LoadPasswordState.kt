package com.jeeldobariya.passcodes.oldui

import com.jeeldobariya.passcodes.database.Password

data class LoadPasswordState(
    val passwordEntityList: List<Password> = emptyList(),
    val isError: Boolean = false
)
