package com.jeeldobariya.passcodes.domain.usecases

import android.content.Context
import android.net.Uri
import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.utils.Constant
import kotlinx.coroutines.flow.first

class ExportPasswordCSVUseCase(
    val context: Context,
    val passwordRepository: PasswordRepository
) {
    suspend fun run(exportFileUri: Uri) {
        context.contentResolver.openOutputStream(exportFileUri)?.bufferedWriter().use { writer ->
            requireNotNull(writer)

            writer.write(Constant.IMPORT_EXPORT_CSV_HEADER)
            writer.newLine()

            passwordRepository.getAllPasswords().first().forEach { password ->
                writer.write("${password.domain},https://local.${password.domain},${password.username},${password.password},${password.notes}")
                writer.newLine()
            }
        }
    }
}
