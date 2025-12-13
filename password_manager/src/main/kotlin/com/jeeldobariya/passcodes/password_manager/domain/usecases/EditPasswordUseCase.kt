package com.jeeldobariya.passcodes.password_manager.domain.usecases

import com.jeeldobariya.passcodes.password_manager.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal

class EditPasswordUseCase(
    val passwordRepository: PasswordRepository
) {
    suspend operator fun invoke(password: PasswordModal) {
        passwordRepository.updatePassword(
            id = password.id,
            domain = password.domain,
            username = password.username,
            password = password.password,
            notes = password.notes
        )
    }
}
