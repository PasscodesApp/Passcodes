package com.jeeldobariya.passcodes.password_manager.presentation.update_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal
import com.jeeldobariya.passcodes.password_manager.domain.usecases.EditPasswordUseCase
import com.jeeldobariya.passcodes.password_manager.domain.usecases.RetrievePasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpdatePasswordViewModel(
    val retrievePasswordUseCase: RetrievePasswordUseCase,
    val editPasswordUseCase: EditPasswordUseCase
) : ViewModel() {

    var passwordEntityId: Int = -1

    private val _state = MutableStateFlow(UpdatePasswordState())
    val state = _state.asStateFlow()

    fun loadInitialData(passwordId: Int) {
        passwordEntityId = passwordId

        viewModelScope.launch {
            try {
                val password: PasswordModal = requireNotNull(retrievePasswordUseCase.run(passwordId))

                _state.update {
                    it.copy(
                        domain = password.domain,
                        username = password.username,
                        password = password.password,
                        notes = password.notes
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(isError = false) }
            }
        }
    }

    fun onAction(action: UpdatePasswordAction) {
        when (action) {
            is UpdatePasswordAction.OnChangeDomain -> { onChangeDomainText(action.newDomain) }
            is UpdatePasswordAction.OnChangeUsername -> { onChangeUsernameText(action.newUsername) }
            is UpdatePasswordAction.OnChangePassword -> { onChangePasswordText(action.newPassword) }
            is UpdatePasswordAction.OnChangeNotes -> { onChangeNotesText(action.newNotes) }
            UpdatePasswordAction.OnUpdatePasswordButtonClick -> { updatePasswordEntity() }
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
                editPasswordUseCase.run(
                    password = PasswordModal(
                        id = passwordEntityId,
                        domain = _state.value.domain,
                        username = _state.value.username,
                        password = _state.value.password,
                        notes = _state.value.notes
                    )
                )
            } catch (_: Exception) {
                _state.update { it.copy(isError = false) }
            }
        }
    }
}
