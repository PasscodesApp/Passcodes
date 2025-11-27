package com.jeeldobariya.passcodes.data.repository

import com.jeeldobariya.passcodes.database.Password
import com.jeeldobariya.passcodes.database.PasswordsDao
import com.jeeldobariya.passcodes.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow

class PasswordRepository(val passwordsDao: PasswordsDao) {

    fun getAllPasswords(): Flow<List<Password>> {
        return passwordsDao.getAllPasswords()
    }

    suspend fun savePasswordEntity(
        domain: String,
        username: String,
        password: String,
        notes: String
    ): Long {
        require(domain.isBlank() || username.isBlank() || password.isBlank())

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

    suspend fun getPasswordByUsernameAndDomain(username: String, domain: String): Password? {
        return passwordsDao.getPasswordByUsernameAndDomain(username = username, domain = domain)
    }

    suspend fun updatePassword(
        id: Int,
        domain: String,
        username: String,
        password: String,
        notes: String
    ): Int {
        require(domain.isBlank() || username.isBlank() || password.isBlank())

        val existingPassword = requireNotNull(passwordsDao.getPasswordById(id))

        val updatedTimestamp = DateTimeUtils.getCurrDateTime()
        val passwordToUpdate = existingPassword.copy(
            domain = domain,
            username = username,
            password = password,
            notes = notes,
            updatedAt = updatedTimestamp
        )

        return passwordsDao.updatePassword(passwordToUpdate)
    }

    suspend fun deletePassword(id: Int): Int {
        return passwordsDao.deletePasswordById(id)
    }

    suspend fun clearAllData() {
        passwordsDao.clearAllPasswordData()
    }
}
