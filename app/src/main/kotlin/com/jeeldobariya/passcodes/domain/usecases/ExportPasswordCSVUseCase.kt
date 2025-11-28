package com.jeeldobariya.passcodes.domain.usecases

import android.content.Context
import android.net.Uri
import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.domain.modals.PasswordModal
import com.jeeldobariya.passcodes.utils.Constant

class ExportPasswordCSVUseCase(
    val context: Context,
    val passwordRepository: PasswordRepository
) {
    suspend fun run(exportFileUri: Uri) {
        context.contentResolver.openOutputStream(exportFileUri)?.bufferedWriter().use { writer ->
            requireNotNull(writer)

            writer.write(Constant.IMPORT_EXPORT_CSV_HEADER)
            writer.newLine()

            passwordRepository.getAllPasswords().forEach { password ->
                writer.write("${password.domain.trim()},https://local.${password.domain.trim()},${password.username.trim()},${password.password.trim()},${password.notes.trim()}")
                writer.newLine()
            }
        }
    }
}
