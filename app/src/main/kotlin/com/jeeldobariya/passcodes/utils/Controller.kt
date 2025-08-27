package com.jeeldobariya.passcodes.utils

import android.content.Context
import com.jeeldobariya.passcodes.database.MasterDatabase
import com.jeeldobariya.passcodes.database.Password
import com.jeeldobariya.passcodes.database.PasswordsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class InvalidInputException(message: String = "Input parameters cannot be blank.") : Exception(message)
class DatabaseOperationException(message: String = "A database operation error occurred.", cause: Throwable? = null) : Exception(message, cause)
class PasswordNotFoundException(message: String = "Password with the given ID was not found.") : Exception(message)
class InvalidImportFormat(message: String = "Given Data Is In Invalid Format") :  Exception(message)

class Controller(context: Context) {
    private val passwordsDao: PasswordsDao

    init {
        // Initialize Room database and get the DAO instance
        val db = MasterDatabase.getDatabase(context)
        passwordsDao = db.passwordsDao
    }

    companion object {
        const val CSV_HEADER = "name,url,username,password,notes"
    }

    /**
     * Saves a new password entity into the database.
     * @return The rowId of the newly inserted row.
     * @throws InvalidInputException if parameters are blank.
     * @throws DatabaseOperationException if a database error occurs.
     */
    suspend fun savePasswordEntity(domain: String, username: String, password: String, notes: String): Long {
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

        return try {
            passwordsDao.insertPassword(newPassword)
        } catch (e: Exception) {
            e.printStackTrace()
            throw DatabaseOperationException("Error saving password.", e)
        }
    }

    /**
     * Retrieves a password entity by its ID.
     * @return The Password object if found.
     * @throws DatabaseOperationException if a database error occurs.
     * @throws PasswordNotFoundException if the password is not found.
     */
    suspend fun getPasswordById(id: Int): Password {
        return try {
            passwordsDao.getPasswordById(id)
                ?: throw PasswordNotFoundException("Password with ID $id not found.")
        } catch (e: PasswordNotFoundException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw DatabaseOperationException("Error retrieving password by ID.", e)
        }
    }

    /**
     * Retrieves a password entity by username and domain.
     * @return The Password object if found.
     * @throws DatabaseOperationException if a database error occurs.
     */
    suspend fun getPasswordByUsernameAndDomain(username: String, domain: String): Password? {
        return try {
            passwordsDao.getPasswordByUsernameAndDomain(username, domain)
        } catch (e: Exception) {
            e.printStackTrace()
            throw DatabaseOperationException("Error retrieving password by username and domain.", e)
        }
    }

    /**
     * Retrieves all password entities from the database as a Flow for real-time updates.
     * @return A Flow that emits lists of Password objects.
     * Room handles the background threading for Flow queries.
     */
    fun getAllPasswords(): Flow<List<Password>> {
        return passwordsDao.getAllPasswords()
    }

    suspend fun updatePassword(id: Int, domain: String, username: String, password: String, notes: String): Int {
        if (domain.isBlank() || username.isBlank() || password.isBlank()) {
            throw InvalidInputException()
        }

        return try {
            val existingPassword = passwordsDao.getPasswordById(id)
                ?: throw PasswordNotFoundException("Password with ID $id not found for update.")

            val updatedTimestamp = DateTimeUtils.getCurrDateTime()
            val passwordToUpdate = existingPassword.copy(
                domain = domain,
                username = username,
                password = password,
                notes = notes,
                updatedAt = updatedTimestamp
            )
            passwordsDao.updatePassword(passwordToUpdate)
        } catch (e: PasswordNotFoundException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw DatabaseOperationException("Error updating password.", e)
        }
    }

    /**
     * Deletes a password entity by its ID.
     * @return The number of rows deleted (usually 1 if successful, 0 if not found).
     * @throws DatabaseOperationException if a database error occurs.
     */
    suspend fun deletePassword(id: Int): Int {
        return try {
            passwordsDao.deletePasswordById(id)
        } catch (e: Exception) {
            e.printStackTrace()
            throw DatabaseOperationException("Error deleting password.", e)
        }
    }

    suspend fun clearAllData() {
        passwordsDao.clearAllPasswordData()
    }

    suspend fun exportDataToCsvString(): String {
        val passwords: List<Password> = getAllPasswords().first()

        val rows = passwords.joinToString("\n") { password ->
            "${password.domain},https://local.${password.domain},${password.username},${password.password},${password.notes}"
        }

        return CSV_HEADER + "\n" + rows
    }

    suspend fun importDataFromCsvString(csvString: String): Int {
        val lines = csvString.lines().filter { it.isNotBlank() }

        if (lines.isEmpty() || lines[0] != CSV_HEADER) {
            throw InvalidImportFormat()
        }

        var importedPasswordCount = 0

        lines.drop(1).forEach { line ->
            val cols = line.split(",")

            try {
                val password: Password? = passwordsDao.getPasswordByUsernameAndDomain(username = cols[2].trim(), domain = cols[0].trim())

                if (password != null) {
                    updatePassword(
                        id = password.id,
                        domain = password.domain,
                        username = password.username,
                        password = cols[3].trim(),
                        notes = cols[4].trim()
                    )
                } else {
                    savePasswordEntity(
                        domain = cols[0].trim(),
                        username = cols[2].trim(),
                        password = cols[3].trim(),
                        notes = cols[4].trim()
                    )
                }

                importedPasswordCount++
            } catch (e: InvalidInputException) {
                e.printStackTrace()
            }
        }

        return importedPasswordCount
    }
}
