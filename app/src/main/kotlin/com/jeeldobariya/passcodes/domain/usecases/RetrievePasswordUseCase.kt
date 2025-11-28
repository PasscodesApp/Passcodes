package com.jeeldobariya.passcodes.domain.usecases

import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.domain.modals.PasswordModal

class RetrievePasswordUseCase(
    val passwordRepository: PasswordRepository
) {
    suspend fun run(id: Int): PasswordModal? {
        return passwordRepository.getPasswordById(id)
    }
}