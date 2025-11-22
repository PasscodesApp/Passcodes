package com.jeeldobariya.passcodes.presentation.view_password

data class ViewPasswordState(
    val domain: String = "",
    val username: String = "",
    val password: String = "",
    val notes: String = "",
    val lastUpdatedAt: String = "",
    val isError: Boolean = false
)
