package com.jeeldobariya.passcodes.oldui

data class UpdatePasswordState(
    val domain: String = "",
    val username: String = "",
    val password: String = "",
    val notes: String = "",
    val isError: Boolean = false
)
