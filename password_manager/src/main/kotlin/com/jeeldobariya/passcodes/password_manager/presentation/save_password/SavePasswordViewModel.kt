package com.jeeldobariya.passcodes.password_manager.presentation.save_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal
import com.jeeldobariya.passcodes.password_manager.domain.usecases.StorePasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavePasswordViewModel(
    val storePasswordUseCase: StorePasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SavePasswordState())
    val state = _state.asStateFlow()

    fun onAction(action: SavePasswordAction) {
        when (action) {
            is SavePasswordAction.OnChangeDomain -> {
                onChangeDomainText(action.newDomain)
            }

            is SavePasswordAction.OnChangeUsername -> {
                onChangeUsernameText(action.newUsername)
            }

            is SavePasswordAction.OnChangePassword -> {
                onChangePasswordText(action.newPassword)
            }

            is SavePasswordAction.OnChangeNotes -> {
                onChangeNotesText(action.newNotes)
            }

            SavePasswordAction.OnSavePasswordButtonClick -> {
                savePasswordEntity()
            }
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

    private fun savePasswordEntity() {
        viewModelScope.launch {
            try {
                storePasswordUseCase(
                    PasswordModal(
                        domain = _state.value.domain,
                        username = _state.value.username,
                        password = _state.value.password,
                        notes = _state.value.notes
                    )
                )
            } catch (e: Exception) {
                _state.update { it.copy(isError = true) }
            }
        }
    }
}
