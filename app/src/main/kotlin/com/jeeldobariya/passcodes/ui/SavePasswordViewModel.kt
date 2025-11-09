package com.jeeldobariya.passcodes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.utils.Controller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavePasswordViewModel(
    val controller: Controller
) : ViewModel() {

    private val _state = MutableStateFlow(SavePasswordState())
    val state = _state.asStateFlow()

    fun onAction(action: SavePasswordAction) {
        when (action) {
            is SavePasswordAction.onChangeDomain -> { onChangeDomainText(action.newDomain) }
            is SavePasswordAction.onChangeUsername -> { onChangeUsernameText(action.newUsername) }
            is SavePasswordAction.onChangePassword -> { onChangePasswordText(action.newPassword) }
            is SavePasswordAction.onChangeNotes -> { onChangeNotesText(action.newNotes) }
            SavePasswordAction.onSavePasswordButtonClick -> { savePasswordEntity() }
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
                controller.savePasswordEntity(
                    _state.value.domain,
                    _state.value.username,
                    _state.value.password,
                    _state.value.notes
                )
            } catch (e: Exception) {
                _state.update { it.copy(isError = true) }
            }
        }
    }
}
