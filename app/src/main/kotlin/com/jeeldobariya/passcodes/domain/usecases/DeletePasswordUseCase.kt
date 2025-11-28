package com.jeeldobariya.passcodes.domain.usecases

import com.jeeldobariya.passcodes.data.repository.PasswordRepository

class DeletePasswordUseCase(
    val passwordRepository: PasswordRepository
) {
    suspend fun run(id: Int) {
        passwordRepository.deletePassword(id = id)
    }
}
