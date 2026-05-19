package com.jeeldobariya.passcodes.password_manager.domain.usecases

import android.content.Context
import android.net.Uri
import com.jeeldobariya.passcodes.password_manager.data.repository.PasswordRepository
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.OutputStreamWriter

class ExportPasswordCSVUseCase(
    val context: Context,
    val passwordRepository: PasswordRepository
) {
    suspend operator fun invoke(exportFileUri: Uri) {
        val outputStream = context.contentResolver.openOutputStream(exportFileUri)
            ?: throw Exception("Failed to open output stream")

        val csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader("name", "url", "username", "password", "notes")
            .get()

        OutputStreamWriter(outputStream).use { writer ->
            CSVPrinter(writer, csvFormat).use { csvPrinter ->
                val passwords = passwordRepository.getAllPasswords()

                for (password in passwords) {
                    val domainClean = password.domain.trim()

                    val url = "https://local.$domainClean"
                    val username = password.username.trim()
                    val passwordString = password.password
                    val notes = password.notes.trim()

                    csvPrinter.printRecord(domainClean, url, username, passwordString, notes)
                }

                csvPrinter.flush()
            }
        }
    }
}
