package com.jeeldobariya.passcodes.password_manager.presentation.password_manager

import android.net.Uri

sealed interface PasswordManagerAction {
    data object RefreshPassword : PasswordManagerAction
    data class OnImportGooglePassword(val fileUri: Uri) : PasswordManagerAction
    data class OnExportGooglePassword(val fileUri: Uri) : PasswordManagerAction
}
