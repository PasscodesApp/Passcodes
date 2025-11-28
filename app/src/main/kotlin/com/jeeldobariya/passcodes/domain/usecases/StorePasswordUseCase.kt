package com.jeeldobariya.passcodes.domain.usecases

import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.domain.modals.PasswordModal

class StorePasswordUseCase(
    val passwordRepository: PasswordRepository
) {
    suspend fun run(password: PasswordModal) {
        passwordRepository.savePasswordEntity(
            domain = password.domain,
            username = password.username,
            password = password.password,
            notes = password.notes
        )
    }
}
