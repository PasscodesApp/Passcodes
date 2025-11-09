package com.jeeldobariya.passcodes.ui

sealed interface SavePasswordAction {
    data class onChangeDomain(val newDomain: String): SavePasswordAction
    data class onChangeUsername(val newUsername: String): SavePasswordAction
    data class onChangePassword(val newPassword: String): SavePasswordAction
    data class onChangeNotes(val newNotes: String): SavePasswordAction
    data object onSavePasswordButtonClick: SavePasswordAction
}
