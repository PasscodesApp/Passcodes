package com.jeeldobariya.passcodes.password_manager.domain.usecases

import com.jeeldobariya.passcodes.password_manager.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal

class RetrieveAllPasswordUseCase(
    val passwordRepository: PasswordRepository
) {
    suspend fun run(): List<PasswordModal> {
        return passwordRepository.getAllPasswords()
    }
}
