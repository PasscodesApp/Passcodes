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

    private val _state = MutableStateFlow(SavePasswordState("", "", "", "", false))
    val state = _state.asStateFlow()

    fun onChangeDomainText(text: String) {
        _state.update { it.copy(domain = text) }
    }

    fun onChangeUsernameText(text: String) {
        _state.update { it.copy(username = text) }
    }

    fun onChangePasswordText(text: String) {
        _state.update { it.copy(password = text) }
    }

    fun onChangeNotesText(text: String) {
        _state.update { it.copy(notes = text) }
    }

    fun onSavePasswordButtonClick() {
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
