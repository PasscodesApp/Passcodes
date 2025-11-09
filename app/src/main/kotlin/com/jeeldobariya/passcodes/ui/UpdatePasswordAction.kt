package com.jeeldobariya.passcodes.ui

sealed interface UpdatePasswordAction {
    data class onChangeDomain(val newDomain: String): UpdatePasswordAction
    data class onChangeUsername(val newUsername: String): UpdatePasswordAction
    data class onChangePassword(val newPassword: String): UpdatePasswordAction
    data class onChangeNotes(val newNotes: String): UpdatePasswordAction
    data object onUpdatePasswordButtonClick: UpdatePasswordAction
}
