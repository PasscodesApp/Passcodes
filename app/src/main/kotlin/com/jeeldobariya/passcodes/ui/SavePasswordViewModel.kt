package com.jeeldobariya.passcodes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.utils.Controller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavePasswordViewModel (
    val controller: Controller
) : ViewModel() {
    private val _domainState = MutableStateFlow("")
    // val domainState = _domainState.asStateFlow()

    private val _usernameState = MutableStateFlow("")
    // val usernameState = _usernameState.asStateFlow()

    private val _passwordState = MutableStateFlow("")
    // val passwordState = _passwordState.asStateFlow()

    private val _notesState = MutableStateFlow("")
    // val notesState = _notesState.asStateFlow()

    private val _isErrorState = MutableStateFlow(false)
    // val isErrorState = _isErrorState.asStateFlow()

    fun onChangeDomainText(text: String) {
        _domainState.update {
            text
        }
    }

    fun onChangeUsernameText(text: String) {
        _usernameState.update {
            text
        }
    }

    fun onChangePasswordText(text: String) {
        _passwordState.update {
            text
        }
    }

    fun onChangeNotesText(text: String) {
        _notesState.update {
            text
        }
    }

    fun onSavePasswordButtonClick() {
        viewModelScope.launch {
            try {
                val rowsAffected = controller.savePasswordEntity(
                    _domainState.value,
                    _usernameState.value,
                    _passwordState.value,
                    _notesState.value
                )
            } catch (e: Exception) {
                _isErrorState.update {
                    true
                }
            }
        }
    }
}
