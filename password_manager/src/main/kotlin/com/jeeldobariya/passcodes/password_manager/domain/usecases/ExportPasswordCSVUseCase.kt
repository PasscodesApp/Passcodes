package com.jeeldobariya.passcodes.password_manager.domain.usecases

import android.content.Context
import android.net.Uri
import com.jeeldobariya.passcodes.password_manager.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal
import com.jeeldobariya.passcodes.password_manager.domain.utils.IMPORT_EXPORT_CSV_HEADER

class ExportPasswordCSVUseCase(
    val context: Context,
    val passwordRepository: PasswordRepository
) {
    suspend fun run(exportFileUri: Uri) {
        context.contentResolver.openOutputStream(exportFileUri)?.bufferedWriter().use { writer ->
            requireNotNull(writer)

            writer.write(IMPORT_EXPORT_CSV_HEADER)
            writer.newLine()

            passwordRepository.getAllPasswords().forEach { password: PasswordModal ->
                writer.write("${password.domain.trim()},https://local.${password.domain.trim()},${password.username.trim()},${password.password.trim()},${password.notes.trim()}")
                writer.newLine()
            }
        }
    }
}
