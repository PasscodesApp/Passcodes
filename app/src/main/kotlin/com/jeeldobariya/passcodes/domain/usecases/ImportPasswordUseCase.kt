package com.jeeldobariya.passcodes.domain.usecases

import android.content.Context
import android.net.Uri
import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.database.Password

class ImportPasswordUseCase(
    val context: Context,
    val passwordRepository: PasswordRepository
) {
    private val CSVHEADER = "name,url,username,password,note"

    suspend fun run(csvFileUri: Uri) {
        context.contentResolver.openInputStream(csvFileUri)?.bufferedReader().use { reader ->
            requireNotNull(reader)

            val header = reader.readLine()
            if (header != CSVHEADER) {
                throw Exception("The given csv file has incorrect header format. Correct Format is [$CSVHEADER]")
            }

            var line: String? = reader.readLine()
            while (line != null) {
                val cols = line.split(",")

                val chosenDomain: String = if (!cols[0].isBlank()) {
                    cols[0].trim()
                } else cols[1].trim()

                val password: Password? = passwordRepository.getPasswordByUsernameAndDomain(
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
