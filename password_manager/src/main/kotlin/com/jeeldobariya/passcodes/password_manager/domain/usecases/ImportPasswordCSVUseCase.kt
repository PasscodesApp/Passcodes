package com.jeeldobariya.passcodes.password_manager.domain.usecases

import android.content.Context
import android.net.Uri
import com.jeeldobariya.passcodes.password_manager.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal
import com.jeeldobariya.passcodes.password_manager.domain.utils.GOGGLE_IMPORT_EXPORT_CSV_HEADER
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStreamReader

class ImportPasswordCSVUseCase(
    val context: Context,
    val passwordRepository: PasswordRepository
) {
    suspend operator fun invoke(importFileUri: Uri) {
        val inputStream = context.contentResolver.openInputStream(importFileUri)
            ?: throw Exception("Failed to open file stream")

        // Configure CSV parser to automatically parse header matching standard formatting
        val csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader() // Dynamically reads first line as header mapping
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .get()

        InputStreamReader(inputStream).use { reader ->
            val parser: CSVParser = csvFormat.parse(reader)

            // 1. Strict Header Validation
            // Google expects keys: "url", "username", "password", "notes" (sometimes "name")
            val headerMap = parser.headerMap
            if (!headerMap.containsKey("url") || !headerMap.containsKey("username") || !headerMap.containsKey("password")) {
                throw Exception("The given CSV file has an incorrect header format. Missing required columns.")
            }

            // 2. Safely process row entries
            for (record in parser) {
                // Extract values by header name mapping instead of hardcoded column indexes
                val url = record.get("url")?.trim().orEmpty()
                val name =
                    if (headerMap.containsKey("name")) record.get("name")?.trim().orEmpty() else ""
                val username = record.get("username")?.trim().orEmpty()
                val passwordString = record.get("password")
                    .orEmpty() // Passwords shouldn't be trimmed to preserve spacing intent
                val notes = if (headerMap.containsKey("notes")) record.get("notes")?.trim()
                    .orEmpty() else ""

                val chosenDomain = url.ifBlank { name }

                // Skip the row if it lacks domain, username, or password
                if (chosenDomain.isBlank() || username.isBlank() || passwordString.isEmpty()) {
                    continue
                }

                // 3. Database Sync Strategy (Check for duplicate)
                val existingPassword: PasswordModal? =
                    passwordRepository.getPasswordByUsernameAndDomain(
                        username = username,
                        domain = chosenDomain
                    )

                if (existingPassword != null) {
                    passwordRepository.updatePassword(
                        id = existingPassword.id,
                        domain = existingPassword.domain,
                        username = existingPassword.username,
                        password = passwordString,
                        notes = notes
                    )
                } else {
                    passwordRepository.savePasswordEntity(
                        domain = chosenDomain,
                        username = username,
                        password = passwordString,
                        notes = notes
                    )
                }
            }
        }
    }
}
