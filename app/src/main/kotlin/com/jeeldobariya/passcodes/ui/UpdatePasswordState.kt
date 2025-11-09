package com.jeeldobariya.passcodes.ui

data class UpdatePasswordState(
    val domain: String = "",
    val username: String = "",
    val password: String = "",
    val notes: String = "",
    val isError: Boolean = false
)
