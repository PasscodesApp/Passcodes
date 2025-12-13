package com.jeeldobariya.passcodes.password_manager.domain.usecases

import com.jeeldobariya.passcodes.password_manager.data.repository.PasswordRepository

class DeletePasswordUseCase(
    val passwordRepository: PasswordRepository
) {
    suspend operator fun invoke(id: Int) {
        passwordRepository.deletePassword(id = id)
    }
}
