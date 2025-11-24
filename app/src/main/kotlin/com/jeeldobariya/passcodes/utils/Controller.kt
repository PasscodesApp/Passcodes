package com.jeeldobariya.passcodes.utils

import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.database.MasterDatabase
import com.jeeldobariya.passcodes.database.Password
import com.jeeldobariya.passcodes.database.PasswordsDao
import kotlinx.coroutines.flow.first

class InvalidInputException(message: String = "Input parameters cannot be blank.") :
    Exception(message)

class InvalidImportFormat(message: String = "Given Data Is In Invalid Format") : Exception(message)

class Controller(database: MasterDatabase, val passwordRepository: PasswordRepository) {
    private val passwordsDao: PasswordsDao = database.passwordsDao

    companion object {
        const val CSV_HEADER = "name,url,username,password,note"
    }



    suspend fun exportDataToCsvString(): String {
        val passwords: List<Password> = passwordRepository.getAllPasswords().first()

        val rows = passwords.joinToString("\n") { password ->
            "${password.domain},https://local.${password.domain},${password.username},${password.password},${password.notes}"
        }

        return CSV_HEADER + "\n" + rows
    }

    suspend fun importDataFromCsvString(csvString: String): IntArray {
        val lines = csvString.lines().filter { it.isNotBlank() }

        if (lines.isEmpty()) {
            throw InvalidImportFormat("Given data seems to be Empty!!")
        }

        if (lines[0] != CSV_HEADER) {
            throw InvalidImportFormat("Given data is not in valid csv format!! correct format:- ${CSV_HEADER}")
        }

        var importedPasswordCount = 0
        var failToImportedPasswordCount = 0

        lines.drop(1).forEach { line ->
            val cols = line.split(",")

            /* NOTE: this need to be done, because our app not allow empty domain. */
            val chosenDomain: String = if (!cols[0].isBlank()) {
                cols[0].trim() // used: name
            } else cols[1].trim() // used: url

            try {
                val password: Password? = passwordsDao.getPasswordByUsernameAndDomain(
                    username = cols[2].trim(),
                    domain = chosenDomain
                )

                if (password != null) {
                    passwordRepository.updatePassword(
                        id = password.id,
                        domain = password.domain,
                        username = password.username,
                        password = cols[3].trim(),
                        notes = cols[4].trim()
                    )
                } else {
                    passwordRepository.savePasswordEntity(
                        domain = chosenDomain,
                        username = cols[2].trim(),
                        password = cols[3].trim(),
                        notes = cols[4].trim()
                    )
                }

                importedPasswordCount++
            } catch (e: InvalidInputException) {
                e.printStackTrace()
                failToImportedPasswordCount++
            }
        }

        return intArrayOf(importedPasswordCount, failToImportedPasswordCount)
    }
}
