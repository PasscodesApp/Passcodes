package com.jeeldobariya.passcodes.domain.usecases

import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.domain.modals.PasswordModal

class RetrieveAllPasswordUseCase(
    val passwordRepository: PasswordRepository
) {
    suspend fun run(): List<PasswordModal> {
        return passwordRepository.getAllPasswords()
    }
}
