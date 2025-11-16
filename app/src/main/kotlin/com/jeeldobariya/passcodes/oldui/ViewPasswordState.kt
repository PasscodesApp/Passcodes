package com.jeeldobariya.passcodes.oldui

data class ViewPasswordState(
    val domain: String = "",
    val username: String = "",
    val password: String = "",
    val notes: String = "",
    val lastUpdatedAt: String = "",
    val isError: Boolean = false
)
