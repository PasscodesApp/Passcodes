package com.jeeldobariya.passcodes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.database.Password
import com.jeeldobariya.passcodes.utils.Controller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpdatePasswordViewModel(
    val controller: Controller
) : ViewModel() {

    var passwordEntityId: Int = -1

    private val _state = MutableStateFlow(UpdatePasswordState())
    val state = _state.asStateFlow()

    fun loadInitialData(passwordId: Int) {
        passwordEntityId = passwordId

        viewModelScope.launch {
            try {
                val password: Password = controller.getPasswordById(passwordId)

                _state.update {
                    it.copy(
                        domain = password.domain,
                        username = password.username,
                        password = password.password,
                        notes = password.notes
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isError = false) }
            }
        }
    }

    fun onAction(action: UpdatePasswordAction) {
        when (action) {
            is UpdatePasswordAction.onChangeDomain -> { onChangeDomainText(action.newDomain) }
            is UpdatePasswordAction.onChangeUsername -> { onChangeUsernameText(action.newUsername) }
            is UpdatePasswordAction.onChangePassword -> { onChangePasswordText(action.newPassword) }
            is UpdatePasswordAction.onChangeNotes -> { onChangeNotesText(action.newNotes) }
            UpdatePasswordAction.onUpdatePasswordButtonClick -> { updatePasswordEntity() }
        }
    }

    private fun onChangeDomainText(newDomain: String) {
        _state.update { it.copy(domain = newDomain) }
    }

    private fun onChangeUsernameText(newUsername: String) {
        _state.update { it.copy(username = newUsername) }
    }

    private fun onChangePasswordText(newPassword: String) {
        _state.update { it.copy(password = newPassword) }
    }

    private fun onChangeNotesText(newNotes: String) {
        _state.update { it.copy(notes = newNotes) }
    }

    private fun updatePasswordEntity() {
        viewModelScope.launch {
            try {
                controller.updatePassword(
                    passwordEntityId,
                    _state.value.domain,
                    _state.value.username,
                    _state.value.password,
                    _state.value.notes
                )
            } catch (e: Exception) {
                _state.update { it.copy(isError = false) }
            }
        }
    }
}
