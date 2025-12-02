package com.jeeldobariya.passcodes.password_manager.presentation.save_password

data class SavePasswordState(
    val domain: String = "",
    val username: String = "",
    val password: String = "",
    val notes: String = "",
    val isError: Boolean = false
)
