package com.jeeldobariya.passcodes.ui

sealed interface UpdatePasswordAction {
    data class OnChangeDomain(val newDomain: String): UpdatePasswordAction
    data class OnChangeUsername(val newUsername: String): UpdatePasswordAction
    data class OnChangePassword(val newPassword: String): UpdatePasswordAction
    data class OnChangeNotes(val newNotes: String): UpdatePasswordAction
    data object OnUpdatePasswordButtonClick: UpdatePasswordAction
}
