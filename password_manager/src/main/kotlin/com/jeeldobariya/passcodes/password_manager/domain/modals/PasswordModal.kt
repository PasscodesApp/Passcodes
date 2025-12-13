package com.jeeldobariya.passcodes.password_manager.domain.modals

data class PasswordModal(
    val id: Int = 0,
    val domain: String,
    val username: String,
    val password: String,
    val notes: String,
    val lastUpdatedAt: String = ""
)
