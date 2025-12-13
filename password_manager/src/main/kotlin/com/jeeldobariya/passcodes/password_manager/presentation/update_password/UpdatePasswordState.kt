package com.jeeldobariya.passcodes.password_manager.presentation.update_password

data class UpdatePasswordState(
    val domain: String = "",
    val username: String = "",
    val password: String = "",
    val notes: String = "",
    val isError: Boolean = false
)
