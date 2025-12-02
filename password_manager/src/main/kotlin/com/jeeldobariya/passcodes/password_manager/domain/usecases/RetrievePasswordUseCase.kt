package com.jeeldobariya.passcodes.password_manager.domain.usecases

import com.jeeldobariya.passcodes.password_manager.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal

class RetrievePasswordUseCase(
    val passwordRepository: PasswordRepository
) {
    suspend fun run(id: Int): PasswordModal? {
        return passwordRepository.getPasswordById(id)
    }
}