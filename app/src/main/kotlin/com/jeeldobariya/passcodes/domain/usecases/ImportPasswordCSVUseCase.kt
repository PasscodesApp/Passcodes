package com.jeeldobariya.passcodes.domain.usecases

import android.content.Context
import android.net.Uri
import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.domain.modals.PasswordModal
import com.jeeldobariya.passcodes.Constant

class ImportPasswordCSVUseCase(
    val context: Context,
    val passwordRepository: PasswordRepository
) {
    suspend fun run(importFileUri: Uri) {
        context.contentResolver.openInputStream(importFileUri)?.bufferedReader().use { reader ->
            requireNotNull(reader)

            val header = reader.readLine()
            if (header != Constant.IMPORT_EXPORT_CSV_HEADER) {
                throw Exception("The given csv file has incorrect header format. Correct Format is [${Constant.IMPORT_EXPORT_CSV_HEADER}]")
            }

            var line: String? = reader.readLine()
            while (line != null) {
                val cols = line.split(",")

                val chosenDomain: String = if (!cols[0].isBlank()) {
                    cols[0].trim()
                } else cols[1].trim()

                // Skip the entity/row of csv
                // If,
                //     It lacks value for either, [domain, username or password]!!
                if (chosenDomain.isBlank() || cols[2].isBlank() || cols[3].isEmpty()) {
                    line = reader.readLine()
                    continue
                }

                val password: PasswordModal? = passwordRepository.getPasswordByUsernameAndDomain(
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

                line = reader.readLine()
            }
        }
    }
}
