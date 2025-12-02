package com.jeeldobariya.passcodes.password_manager.data.repository

import com.jeeldobariya.passcodes.database.master.PasswordEntity
import com.jeeldobariya.passcodes.database.master.PasswordsDao
import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal
import com.jeeldobariya.passcodes.password_manager.domain.utils.DateTimeUtils
import kotlinx.coroutines.flow.first
import kotlin.require

class PasswordRepository(val passwordsDao: PasswordsDao) {

    suspend fun getAllPasswords(): List<PasswordModal> {
        return passwordsDao.getAllPasswords().first().map {
            PasswordModal(
                id = it.id,
                domain = it.domain,
                username = it.username,
                password = it.password,
                notes = it.notes,
                lastUpdatedAt = DateTimeUtils.getRelativeDays(it.updatedAt.orEmpty())
            )
        }
    }

    suspend fun savePasswordEntity(
        domain: String,
        username: String,
        password: String,
        notes: String
    ): Int {
        require(domain.isNotBlank() || username.isNotBlank() || password.isNotBlank())
        val currentTimestamp = DateTimeUtils.getCurrDateTime()
        val newPassword = PasswordEntity(
            domain = domain,
            username = username,
            password = password,
            notes = notes,
            createdAt = currentTimestamp,
            updatedAt = currentTimestamp
        )

        return passwordsDao.insertPassword(newPassword).toInt()
    }

    suspend fun getPasswordById(id: Int): PasswordModal? {
        val result = passwordsDao.getPasswordById(id)

        return if (result != null) {
            PasswordModal(
                id = result.id,
                domain = result.domain,
                username = result.username,
                password = result.password,
                notes = result.notes,
                lastUpdatedAt =  DateTimeUtils.getRelativeDays(result.updatedAt.orEmpty())
            )
        } else {
            null
        }
    }

    suspend fun getPasswordByUsernameAndDomain(username: String, domain: String): PasswordModal? {
        val result = passwordsDao.getPasswordByUsernameAndDomain(username = username, domain = domain)

        return if (result != null) {
            PasswordModal(
                id = result.id,
                domain = result.domain,
                username = result.username,
                password = result.password,
                notes = result.notes,
                lastUpdatedAt = "TODO()"
            )
        } else {
            null
        }
    }

    suspend fun updatePassword(
        id: Int,
        domain: String,
        username: String,
        password: String,
        notes: String
    ): Int {
        require(domain.isNotBlank() || username.isNotBlank() || password.isNotBlank())

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

    suspend fun clearAllData(): Unit {
        passwordsDao.clearAllPasswordData()
    }
}
