package com.jeeldobariya.passcodes.domain.modals

data class PasswordModal(
    val id: Int,
    val domain: String,
    val username: String,
    val password: String,
    val notes: String,
    val lastUpdatedAt: String
)
