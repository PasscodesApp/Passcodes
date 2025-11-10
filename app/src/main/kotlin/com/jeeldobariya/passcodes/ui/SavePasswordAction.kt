package com.jeeldobariya.passcodes.ui

sealed interface SavePasswordAction {
    data class OnChangeDomain(val newDomain: String): SavePasswordAction
    data class OnChangeUsername(val newUsername: String): SavePasswordAction
    data class OnChangePassword(val newPassword: String): SavePasswordAction
    data class OnChangeNotes(val newNotes: String): SavePasswordAction
    data object OnSavePasswordButtonClick: SavePasswordAction
}
