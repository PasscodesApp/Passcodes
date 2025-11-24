package com.jeeldobariya.passcodes.presentation.update_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import com.jeeldobariya.passcodes.database.Password
import com.jeeldobariya.passcodes.utils.Controller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpdatePasswordViewModel(
    val passwordRepository: PasswordRepository
) : ViewModel() {

    var passwordEntityId: Int = -1

    private val _state = MutableStateFlow(UpdatePasswordState())
    val state = _state.asStateFlow()

    fun loadInitialData(passwordId: Int) {
        passwordEntityId = passwordId

        viewModelScope.launch {
            try {
                val password: Password = requireNotNull(passwordRepository.getPasswordById(passwordId))

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
                passwordRepository.updatePassword(
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
