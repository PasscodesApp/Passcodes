package com.jeeldobariya.passcodes.data.repository

import com.jeeldobariya.passcodes.database.Password
import com.jeeldobariya.passcodes.database.PasswordsDao
import com.jeeldobariya.passcodes.utils.DateTimeUtils
import com.jeeldobariya.passcodes.utils.InvalidInputException

class PasswordRepository(val passwordsDao: PasswordsDao) {
    suspend fun savePasswordEntity(
        domain: String,
        username: String,
        password: String,
        notes: String
    ): Long {
        if (domain.isBlank() || username.isBlank() || password.isBlank()) {
            throw InvalidInputException()
        }

        val currentTimestamp = DateTimeUtils.getCurrDateTime()
        val newPassword = Password(
            domain = domain,
            username = username,
            password = password,
            notes = notes,
            createdAt = currentTimestamp,
            updatedAt = currentTimestamp
        )

        return passwordsDao.insertPassword(newPassword)
    }

    suspend fun getPasswordById(id: Int): Password? {
        return passwordsDao.getPasswordById(id)
    }


}
