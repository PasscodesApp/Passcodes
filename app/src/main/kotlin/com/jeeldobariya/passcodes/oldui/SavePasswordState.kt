package com.jeeldobariya.passcodes.oldui

data class SavePasswordState(
    val domain: String = "",
    val username: String = "",
    val password: String = "",
    val notes: String = "",
    val isError: Boolean = false
)
