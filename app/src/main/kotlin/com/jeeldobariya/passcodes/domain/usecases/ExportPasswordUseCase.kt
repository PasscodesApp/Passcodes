package com.jeeldobariya.passcodes.domain.usecases

import android.content.Context
import android.net.Uri
import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.database.Password
import kotlinx.coroutines.flow.first

class ExportPasswordUseCase(
    val context: Context,
    val passwordRepository: PasswordRepository
) {
    private val CSVHEADER = "name,url,username,password,note"

    suspend fun run(exportFileUri: Uri) {
        context.contentResolver.openOutputStream(exportFileUri)?.bufferedWriter().use { writer ->
            requireNotNull(writer)

            writer.write(CSVHEADER)

            val passwords: List<Password> = passwordRepository.getAllPasswords().first()

            passwords.forEach { password ->
                val row = "${password.domain},https://local.${password.domain},${password.username},${password.password},${password.notes}"
                writer.write(row)
                writer.newLine()
            }
        }
    }
}
